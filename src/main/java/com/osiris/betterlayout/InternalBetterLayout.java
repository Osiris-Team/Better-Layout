/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout;

import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
            Insets insets = container.getInsets();
            int startX = insets.left;
            int startY = insets.top;
            int x = startX;
            int y = startY;
            // To avoid memory filling up with leftover (already removed) components
            // we replace the original compsAndStyles map after being done,
            // with the map below, that only contains currently added/active components.
            Map<Component, Styles> newCompsAndStyles = new HashMap<>();
            //System.err.println("\n\nLOOP FOR NEW CONTAINER: " + container.getClass().getSimpleName() + "/" + Integer.toHexString(container.hashCode()) +" startX="+startX+" startY="+startY);
            Component[] components = container.getComponents();
            for (int i = 0; i < components.length; i++) {
                Component comp = components[i];
                Styles styles = container.compsAndStyles.get(comp);
                if (styles == null) {
                    styles = new Styles(); // Components added via the regular container add() methods
                    styles.getMap().putAll(container.defaultCompStyles.getMap()); // Add defaults
                }
                newCompsAndStyles.put(comp, styles);

                if (comp.isVisible()) {
                    //System.err.println("\ncomp: " + comp.getClass().getSimpleName() + "/" + Integer.toHexString(comp.hashCode()));
                    Dimension compSize = comp.getSize();
                    Dimension compPrefSize = comp.getPreferredSize();
                    if (compSize.width < compPrefSize.width || compSize.height < compPrefSize.height)
                        compSize = compPrefSize;
                    int totalWidth = compSize.width;
                    int totalHeight = compSize.height;
                    byte paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
                    //System.err.println("at start: " + x + "x " + y + "y " + totalWidth + "w " + totalHeight + "h ");

                    // Calc total width and height
                    for (Map.Entry<String, String> entry : styles.getMap().entrySet()) {
                        String key, value;
                        try {
                            key = entry.getKey();
                            value = entry.getValue();
                        } catch (IllegalStateException ise) {
                            // this usually means the entry is no longer in the map.
                            throw new ConcurrentModificationException(ise);
                        }
                        // Calc total height and width
                        if (Objects.equals(key, Style.padding_left.key)) {
                            paddingLeft = Byte.parseByte(value);
                            totalWidth += paddingLeft;
                        } else if (Objects.equals(key, Style.padding_right.key)) {
                            paddingRight = Byte.parseByte(value);
                            totalWidth += paddingRight;
                        } else if (Objects.equals(key, Style.padding_top.key)) {
                            paddingTop = Byte.parseByte(value);
                            totalHeight += paddingTop;
                        } else if (Objects.equals(key, Style.padding_bottom.key)) {
                            paddingBottom = Byte.parseByte(value);
                            totalHeight += paddingBottom;
                        }
                    }
                    // Align the component either vertically or horizontally
                    boolean isHorizontal = isHorizontal(styles);
                    styles.debugInfo = new DebugInfo(isHorizontal, totalWidth, totalHeight, paddingLeft, paddingRight, paddingTop, paddingBottom);
                    //System.err.println("totalWidth: " + totalWidth + " totalHeight: " + totalHeight);

                    if (!isHorizontal) { // CURRENT COMP IS VERTICAL
                        int totalHeightTallestCompInRow = 0;
                        for (int j = i - 1; j >= 0; j--) {
                            if (components[j].isVisible()) {
                                Styles stylesBefore = newCompsAndStyles.get(components[j]);
                                if (stylesBefore.debugInfo.totalHeight > totalHeightTallestCompInRow)
                                    totalHeightTallestCompInRow = stylesBefore.debugInfo.totalHeight;
                                if (!stylesBefore.debugInfo.isHorizontal) break;
                            }
                        }
                        //System.err.println("IS VERTICAL! : " + x + "x " + y + "y " + totalHeightTallestCompInRow + "tallestHeight");
                        x = startX; // Move comp to the left (start)
                        y += totalHeightTallestCompInRow; // Move comp to the next line
                        totalHeightTallestCompInRow = totalHeight; // Directly update variable since we are now in the next row
                        //System.err.println("IS VERTICAL! : " + x + "x " + y + "y " + totalHeightTallestCompInRow + "tallestHeight");
                    }

                    // Set the component's size and position.
                    comp.setBounds(x + paddingLeft, y + paddingTop, compSize.width, compSize.height);
                    // Set the next components start positions.
                    x += totalWidth;
                    //System.err.println("at end: " + x + "x " + y + "y " + totalWidth + "w " + totalHeight + "h ");
                }
            }
            // Reset x,y after loop
            x = startX;
            y = startY;
            if (container.isCropToContent) {
                int insideHeight = 0, insideWidth = 0;
                int widthLastRow = 0, heightLastRow = 0;
                //System.err.println("start: "+x+"x "+y+"y "+insideWidth+"w "+insideHeight+"h");
                for (Map.Entry<Component, Styles> entry : newCompsAndStyles.entrySet()) {
                    Component key_comp;
                    Styles value_styles;
                    try {
                        key_comp = entry.getKey();
                        value_styles = entry.getValue();
                    } catch (IllegalStateException ise) { // this usually means the entry is no longer in the map.
                        throw new ConcurrentModificationException(ise);
                    }
                    if (key_comp.isVisible()) {
                        if (isHorizontal(value_styles)) {
                            widthLastRow += value_styles.debugInfo.totalWidth;
                            if (heightLastRow < value_styles.debugInfo.totalHeight)
                                heightLastRow = value_styles.debugInfo.totalHeight;
                            //System.err.println("horizontal, width+="+value_styles.debugInfo.totalWidth);
                        } else {
                            heightLastRow += value_styles.debugInfo.totalHeight;
                            widthLastRow = value_styles.debugInfo.totalWidth;
                            //System.err.println("vertical, height+="+value_styles.debugInfo.totalHeight);
                        }
                        if (widthLastRow > insideWidth) insideWidth = widthLastRow;
                        if (heightLastRow > insideHeight) insideHeight = heightLastRow;
                    }
                    //System.err.println("end loop ->");
                }
                //System.err.println("\n\ncontainer: "+x+"x "+y+"y "+insideWidth+"w "+insideHeight+"h");
                Rectangle boundsNow = container.getBounds();
                //System.err.println("container-bounds: "+boundsNow.x+"x "+boundsNow.y+"y "+boundsNow.width+"w "+boundsNow.height+"h");
                if (boundsNow.x < x || boundsNow.y < y || boundsNow.width < insideWidth || boundsNow.height < insideHeight) {
                    // Check above is required to prevent infinite loop when inside another container.
                    // Specially when inside a ScrollPane for example.
                    container.setBounds(x, y, insideWidth, insideHeight);
                    updateSizes(container, insideWidth, insideHeight);
                }
            }
            container.compsAndStyles = newCompsAndStyles;
            if (container.isDebug) drawDebugLines(container); // Must be done after replacing the map
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

    private boolean isHorizontal(Styles styles) {
        String alignment = styles.getMap().get(Style.vertical.key);
        if (alignment == null) alignment = Style.horizontal.value;
        return Objects.equals(alignment, Style.horizontal.value);
    }


    private void drawDebugLines(BLayout container) {
        Graphics2D g = (Graphics2D) container.getGraphics();
        if (g == null) return;
        for (Component comp : container.getComponents()) {
            Styles styles = container.compsAndStyles.get(comp);
            Objects.requireNonNull(styles);
            int x = comp.getX() - styles.debugInfo.paddingLeft;
            int y = comp.getY() - styles.debugInfo.paddingTop;
            int width = comp.getWidth() + styles.debugInfo.paddingLeft + styles.debugInfo.paddingRight;
            int height = comp.getHeight() + styles.debugInfo.paddingTop + styles.debugInfo.paddingBottom;
            g.setColor(Color.red);
            g.drawRect(x, y, width, height); // Full width/height with padding included
            g.setColor(Color.blue); // Actual component width/height
            g.drawRect(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight());
        }
    }
}
