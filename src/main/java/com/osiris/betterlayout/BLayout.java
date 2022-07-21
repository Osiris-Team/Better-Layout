/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout;

import com.osiris.betterlayout.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Magical container that makes layouting
 * as simple as writing basic english. <p>
 */
public class BLayout extends JPanel {
    public Container parent;
    /**
     * Default child component styles. <br>
     */
    public Styles defaultCompStyles = new Styles().center().padding();
    /**
     * Maps child components to their styles.
     */
    public Map<Component, Styles> compsAndStyles = new HashMap<>();
    /**
     * Call {@link #refresh()} to see the changes on the UI. <br>
     * Enables debug lines that display corners and padding of each child component. <br>
     */
    public boolean isDebug = false;
    /**
     * Call {@link #refresh()} to see the changes on the UI. <br>
     * Container size gets set to the total child components size. <br>
     */
    public boolean isCropToContent = false;
    private JScrollPane scrollPane = null;

    /**
     * Defaults width & height to 100% of the WINDOW.
     */
    public BLayout() {
        this(null);
    }

    /**
     * Defaults width & height to 100% of the PARENT.
     */
    public BLayout(Container parent) {
        this(parent, 100, 100);
    }

    /**
     * Crops to the content of the container, aka
     * the total width & height of all its child components. <br>
     * If false, defaults width & height to 0%.
     *
     * @see #isCropToContent
     */
    public BLayout(Container parent, boolean isCropToContent) {
        this(parent, 0, 0);
        this.isCropToContent = isCropToContent;
    }

    public BLayout(Container parent, int widthPercent, int heightPercent) {
        super(new InternalBetterLayout(new Dimension(0, 0)));
        this.parent = parent;
        // transparent NOPE, this makes other windows to show, thus we use the parent color
        updateSize(widthPercent, heightPercent);
    }

    /**
     * Call {@link #refresh()} to see the changes on the UI. <br>
     */
    public void updateSize(int widthPercent, int heightPercent) {
        int parentWidth, parentHeight;
        if (parent != null) {
            parentWidth = parent.getWidth();
            parentHeight = parent.getHeight();
        } else { // If no parent provided use the screen dimensions
            parentWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            parentHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        }

        Dimension size = new Dimension(parentWidth / 100 * widthPercent,
                parentHeight / 100 * heightPercent);

        // Update layout sizes
        ((InternalBetterLayout) getLayout()).minimumSize = size;
        ((InternalBetterLayout) getLayout()).preferredSize = size;

        // Update container sizes
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    public void refresh() {
        UIUtils.refresh(this);
    }

    /**
     * Access this container in a thread-safe way. <br>
     * Performs {@link UIUtils#revalidateAllUp(Component, boolean)} when done running the provided code. <p>
     * <p>
     * Note that there is no need of calling {@link #access(Runnable)} again in
     * child containers that are used in the provided code.
     *
     * @param code to be run in this containers' context.
     */
    public synchronized BLayout access(Runnable code) {
        code.run();
        UIUtils.revalidateAllUp(this);
        return this;
    }

    /**
     * Adds this component horizontally and
     * additionally returns its {@link Styles}. <p>
     * Its {@link Styles} are pre-filled with
     * {@link #defaultCompStyles} of this container.
     */
    public Styles addH(Component comp) {
        super.add(comp);
        Styles styles = new Styles();
        styles.getMap().putAll(defaultCompStyles.getMap()); // Add defaults
        styles.horizontal();
        compsAndStyles.put(comp, styles);
        return styles;
    }

    /**
     * @see #addV(Component)
     */
    public BLayout addH(Component... components) {
        for (Component component : components) {
            addV(component);
        }
        return this;
    }

    /**
     * Adds this component vertically and
     * additionally returns its {@link Styles}.<p>
     * Its {@link Styles} are pre-filled with
     * {@link #defaultCompStyles} of this container.
     */
    public Styles addV(Component comp) {
        super.add(comp);
        Styles styles = new Styles();
        styles.getMap().putAll(defaultCompStyles.getMap()); // Add defaults
        styles.vertical();
        compsAndStyles.put(comp, styles);
        return styles;
    }

    /**
     * @see #addV(Component)
     */
    public BLayout addV(Component... components) {
        for (Component component : components) {
            addV(component);
        }
        return this;
    }

    /**
     * @throws IllegalArgumentException when provided layout
     *                                  not of type {@link InternalBetterLayout}.
     */
    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr instanceof InternalBetterLayout)
            super.setLayout(mgr);
        else
            throw new IllegalArgumentException("Layout must be of type: " + InternalBetterLayout.class.getName());
    }

    /**
     * Returns the styles for the provided child component.
     */
    public Styles getChildStyles(Component comp) {
        return compsAndStyles.get(comp);
    }

    /**
     * Removes this layout from its parent and replaces it with a {@link JScrollPane}.
     * Sets {@link #isCropToContent} to true
     * and adds this {@link BLayout} to the {@link JScrollPane}. <p>
     *
     * Note that the {@link #parent} variable will not get updated, since
     * it's required to stay the same for {@link #makeUnscrollable()}.
     *
     * @throws NullPointerException if {@link #parent} is null.
     */
    public BLayout makeScrollable() {
        Objects.requireNonNull(parent);
        parent.remove(this);
        JScrollPane scrollPane = new JScrollPane(this);
        this.scrollPane = scrollPane;
        this.isCropToContent = true;
        //scrollPane.setLayout(new FixScrollPaneLayout.UIResource());
        scrollPane.setPreferredSize(this.getPreferredSize());
        parent.add(scrollPane);
        return this;
    }

    public BLayout makeUnscrollable(){
        if(scrollPane == null) return this;
        int i = 0;
        synchronized (parent.getTreeLock()) {
            for (Component c : parent.getComponents()) {
                if(Objects.equals(c, scrollPane)){
                    break;
                }
                i++;
            }
        }
        parent.remove(scrollPane);
        parent.add(this, i);
        return this;
    }

    public BLayout scrollToEndV() {
        if (scrollPane == null) return this;
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
        return this;
    }

    public BLayout scrollToStartV() {
        if (scrollPane == null) return this;
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setValue(bar.getMinimum());
        return this;
    }

    public BLayout scrollToEndH() {
        if (scrollPane == null) return this;
        JScrollBar bar = scrollPane.getHorizontalScrollBar();
        bar.setValue(bar.getMaximum());
        return this;
    }

    public BLayout scrollToStartH() {
        if (scrollPane == null) return this;
        JScrollBar bar = scrollPane.getHorizontalScrollBar();
        bar.setValue(bar.getMinimum());
        return this;
    }

}
