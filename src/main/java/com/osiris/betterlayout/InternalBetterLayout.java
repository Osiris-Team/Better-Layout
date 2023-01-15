/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout;

import com.osiris.betterlayout.utils.StyledComponent;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Note that this class is private to classes outside this package, due to it
 * being specifically designed for {@link BLayout} and thus not being compatible with other containers.
 * If used on another container you probably will get {@link ClassCastException}s. <p>
 * <p>
 * Features: <br>
 * - Ensures the container never expands if the components require more space.
 * This is done by overriding the container max size at {@link #preferredLayoutSize(Container)}. <br>
 */
class InternalBetterLayout implements LayoutManager {
    public int minWidth = 0, minHeight = 0;
    public int preferredWidth = 0, preferredHeight = 0;
    public Dimension minimumSize, preferredSize;
    int startX, startY;
    private Dimension containerSize, containerPrefSize;

    public InternalBetterLayout(Dimension size) {
        this(size, size);
    }

    public InternalBetterLayout(Dimension minimumSize, Dimension preferredSize) {
        this.minimumSize = minimumSize;
        this.preferredSize = preferredSize;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container _container) {
        // Not 100% safe this method is always called, thus nothing important being done here.
        return preferredSize;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        // Not 100% safe this method is always called, thus nothing important being done here.
        return minimumSize;
    }

    @Override
    public void layoutContainer(Container _container) {
        BLayout container = (BLayout) _container;
        synchronized (container.getTreeLock()) {
            //container.setMaximumSize(preferredSize); // Make sure maximum is never bigger than preferred.
            containerSize = container.getSize();
            containerPrefSize = container.getPreferredSize();
            if (containerSize.width < containerPrefSize.width || containerSize.height < containerPrefSize.height)
                containerSize = containerPrefSize;
            Insets insets = container.getInsets();
            startX = insets.left;
            startY = insets.top;
            // To avoid memory filling up with leftover (already removed) components
            // we replace the original compsAndStyles map after being done,
            // with the map below, that only contains currently added/active components.
            Map<Component, CompWrapper> newCompsAndStyles = new HashMap<>();
            //System.err.println("\n\nLOOP FOR NEW CONTAINER: " + container.getClass().getSimpleName() + "/" + Integer.toHexString(container.hashCode()) +" startX="+startX+" startY="+startY);
            Component[] components = container.getComponents();
            for (Component comp : components) {
                CompWrapper compWrapper = container.compsAndStyles.get(comp);
                if (compWrapper == null) {
                    compWrapper = new CompWrapper(comp); // Components added via the regular container add() methods
                    compWrapper.map.putAll(container.defaultCompCompWrapper.map); // Add defaults
                }
                newCompsAndStyles.put(comp, compWrapper);
            }
            container.compsAndStyles = newCompsAndStyles;
            java.util.List<StyledComponent[]> rows = toRows(newCompsAndStyles, components);
            drawRows(rows);

            if (container.isCropToContent) {
                //System.err.println("\n\ncontainer: "+x+"x "+y+"y "+insideWidth+"w "+insideHeight+"h");
                Rectangle boundsNow = container.getBounds();
                int containerWidth = calcContainerWidth(rows);
                int containerHeight = calcContainerHeight(rows);
                //System.err.println("container-bounds: "+boundsNow.x+"x "+boundsNow.y+"y "+boundsNow.width+"w "+boundsNow.height+"h");
                if (boundsNow.width < containerWidth || boundsNow.height < containerHeight) {
                    // Check above is required to prevent infinite loop when inside another container.
                    // Specially when inside a ScrollPane for example.
                    container.setBounds(boundsNow.x, boundsNow.y, containerWidth, containerHeight);
                    updateSizes(container, containerWidth, containerHeight);
                }
            }
            if (container.isDebug) drawDebugLines(container); // Must be done after replacing the map
        }
    }

    private int calcContainerHeight(java.util.List<StyledComponent[]> rows) {
        int height = 0;
        for (StyledComponent[] row : rows) {
            if (row.length != 0)
                height += row[0].compWrapper.info.totalHeight; // == rowHeight == the tallest component
        }
        return height;
    }

    private int calcContainerWidth(java.util.List<StyledComponent[]> rows) {
        int width = 0;
        for (StyledComponent[] row : rows) {
            int rowWidth = 0;
            for (StyledComponent styledComponent : row) {
                rowWidth += styledComponent.compWrapper.info.totalWidth;
            }
            if (rowWidth > width) width = rowWidth;
        }
        return width;
    }

    private void drawRows(java.util.List<StyledComponent[]> rows) {
        int x = startX;
        int y = startY;
        for (StyledComponent[] row : rows) {

            // DETERMINE TOTAL HEIGHT & WIDTH FOR EACH COMPONENT IN ROW
            for (StyledComponent styledComponent : row) {
                Component comp = styledComponent.component;
                CompWrapper compWrapper = styledComponent.compWrapper;
                Dimension compSize = comp.getSize();
                Dimension compPrefSize = comp.getPreferredSize();
                if (compSize.width < compPrefSize.width || compSize.height < compPrefSize.height)
                    compSize = compPrefSize;
                int width = compSize.width;
                int height = compSize.height;
                int totalWidth = compSize.width;
                int totalHeight = compSize.height;
                byte paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
                //System.err.println("at start: " + x + "x " + y + "y " + totalWidth + "w " + totalHeight + "h ");
                String valPaddingLeft = compWrapper.map.get(Style.padding_left.key);
                String valPaddingRight = compWrapper.map.get(Style.padding_right.key);
                String valPaddingTop = compWrapper.map.get(Style.padding_top.key);
                String valPaddingBottom = compWrapper.map.get(Style.padding_bottom.key);
                if (valPaddingLeft != null) {
                    paddingLeft = Byte.parseByte(valPaddingLeft);
                    totalWidth += paddingLeft;
                }
                if (valPaddingRight != null) {
                    paddingRight = Byte.parseByte(valPaddingRight);
                    totalWidth += paddingRight;
                }
                if (valPaddingTop != null) {
                    paddingTop = Byte.parseByte(valPaddingTop);
                    totalHeight += paddingTop;
                }
                if (valPaddingBottom != null) {
                    paddingBottom = Byte.parseByte(valPaddingBottom);
                    totalHeight += paddingBottom;
                }
                // If component 100% width or height, make it smaller to prevent padding overflow to the right or bottom
                /* // Doesn't work like intended somehow //TODO
                if(totalWidth >= containerSize.width && (paddingLeft != 0 || paddingRight != 0)){
                    width = compSize.width - (paddingLeft + paddingRight);
                    totalWidth -= (paddingLeft + paddingRight);
                }
                if(totalHeight >= containerSize.height && (paddingTop != 0 || paddingBottom != 0)) {
                    height = compSize.height - (paddingTop + paddingBottom);
                    totalHeight -= (paddingTop + paddingBottom);
                }
                 */
                compWrapper.info.width = width;
                compWrapper.info.height = height;
                compWrapper.info.totalWidth = totalWidth;
                compWrapper.info.totalHeight = totalHeight;
                compWrapper.info.paddingLeft = paddingLeft;
                compWrapper.info.paddingRight = paddingRight;
                compWrapper.info.paddingTop = paddingTop;
                compWrapper.info.paddingBottom = paddingBottom;
            }

            // DETERMINE TOTAL HEIGHT OF TALLEST COMPONENT IN ROW
            int rowHeight = 0;
            for (StyledComponent styledComponent : row) {
                if (styledComponent.compWrapper.info.totalHeight > rowHeight)
                    rowHeight = styledComponent.compWrapper.info.totalHeight;
            }
            // Set the total height of all components in the row, to the tallest height
            for (StyledComponent styledComponent : row) {
                styledComponent.compWrapper.info.totalHeight = rowHeight;
            }
            // fill()

            // Determine position

            // DRAW COMPONENTS IN ROW
            for (StyledComponent styledComponent : row) {
                // Set the component's size and position.
                styledComponent.component.setBounds(
                        x + styledComponent.compWrapper.info.paddingLeft, y + styledComponent.compWrapper.info.paddingTop,
                        styledComponent.compWrapper.info.width, styledComponent.compWrapper.info.height);
                //System.err.println("draw("+(styledComponent.styles.info.isHorizontal ? "H":"V")+"): "+styledComponent.component.getClass().getSimpleName() + "/" + Integer.toHexString(styledComponent.component.hashCode()) + " "+
                //        x + styledComponent.styles.info.paddingLeft+ "x " + styledComponent.styles.info.width + "width "+ y + styledComponent.styles.info.paddingTop + "y " + styledComponent.styles.info.height + "height ");
                x += styledComponent.compWrapper.info.totalWidth; // For the next components start position.
            }
            // Next row, aka new line
            x = startX;
            y += rowHeight; // Move comp to the next line
        }
    }

    /**
     * Updates the other secondary sizes of a component.
     */
    private void updateSizes(Component comp, int width, int height) {
        comp.setPreferredSize(new Dimension(width, height));
        comp.setMaximumSize(new Dimension(width, height));
        comp.setMinimumSize(new Dimension(width, height));
        // Won't work somehow:
//        comp.getPreferredSize().width = width;
//        comp.getPreferredSize().height = height;
//        comp.getMaximumSize().width = width;
//        comp.getMaximumSize().height = height;
//        comp.getMinimumSize().width = width;
//        comp.getMinimumSize().height = height;
    }

    /**
     * Goes through the provided components array and searches for vertical
     * components. Those components can be thought of \n (next line) characters.
     * When a vertical component is found, all components until the start or
     * last vertical component get put into a new array. That's our row.
     * <p>
     * This method returns a list of those rows. Besides, that is also does: <br>
     * - Only visible components are added to the row. <br>
     * - Styles are mapped to components, aka the {@link StyledComponent} obj is created. <br>
     * - {@link CompWrapper#info} is initialized, and the value for {@link DebugInfo#isHorizontal} set. <br>
     *
     * @param _components   not null.
     * @param mapCompStyles not null, and expected to have one {@link CompWrapper} object, for each {@link Component} object
     *                      in the array.
     */
    private java.util.List<StyledComponent[]> toRows(Map<Component, CompWrapper> mapCompStyles, Component... _components) {
        // Remove not visible components first
        List<StyledComponent> temp = new ArrayList<>(_components.length);
        for (Component comp : _components) {
            if (comp.isVisible()) {
                StyledComponent styledComponent = new StyledComponent(comp);
                styledComponent.compWrapper = mapCompStyles.get(comp);
                styledComponent.compWrapper.info = new DebugInfo();
                styledComponent.compWrapper.info.isHorizontal = isHorizontal(styledComponent.compWrapper);
                temp.add(styledComponent);
            }
        }
        StyledComponent[] components = temp.toArray(new StyledComponent[0]);

        // Do actual job:
        java.util.List<StyledComponent[]> rows = new ArrayList<>();
        int lastVerticalCompIndex = 0;
        for (int i = 0; i < components.length; i++) {
            StyledComponent comp = components[i];
            if (!comp.compWrapper.info.isHorizontal) {
                rows.add(Arrays.copyOfRange(components, lastVerticalCompIndex, i));  // i exclusive
                lastVerticalCompIndex = i;
            }
        }
        if (components.length > 0 && !components[components.length - 1].compWrapper.info.isHorizontal)// Last vertical component
            rows.add(new StyledComponent[]{components[components.length - 1]});
        if (rows.isEmpty()) { // Only horizontal components
            rows.add(components);
        }
        return rows;
    }

    private boolean isVertical(CompWrapper compWrapper) {
        return !isHorizontal(compWrapper);
    }

    private boolean isHorizontal(CompWrapper compWrapper) {
        String alignment = compWrapper.map.get(Style.vertical.key);
        if (alignment == null) alignment = Style.horizontal.value;
        return Objects.equals(alignment, Style.horizontal.value);
    }


    private void drawDebugLines(BLayout container) {
        Graphics2D g = (Graphics2D) container.getGraphics();
        if (g == null) return;
        for (Component comp : container.getComponents()) {
            CompWrapper compWrapper = container.compsAndStyles.get(comp);
            Objects.requireNonNull(compWrapper);
            int x = comp.getX() - compWrapper.info.paddingLeft;
            int y = comp.getY() - compWrapper.info.paddingTop;
            int width = comp.getWidth() + compWrapper.info.paddingLeft + compWrapper.info.paddingRight;
            int height = comp.getHeight() + compWrapper.info.paddingTop + compWrapper.info.paddingBottom;
            g.setColor(Color.red);
            g.drawRect(x, y, width, height); // Full width/height with padding included
            g.setColor(Color.blue); // Actual component width/height
            g.drawRect(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight());
        }
    }
}
