/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper around a {@link Style}s map that
 * provides util methods for setting and retrieving stuff.
 */
public class Styles {
    /**
     * Map containing the actual styles.
     *
     * @see Style
     */
    public Map<String, String> map;
    /**
     * Contains details relevant to debugging
     * layouts and components.
     * Usually gets updated/set inside the layout class.
     *
     * @see InternalBetterLayout
     */
    public DebugInfo debugInfo;

    public Component component;

    public Styles() {
        this(null, null);
    }

    public Styles(Component component) {
        this(component, null);
    }

    public Styles(Component component, Map<String, String> map) {
        this.component = component;
        if(map == null) this.map = new HashMap<>();
        else this.map = map;
    }

    // ALIGNMENT

    public Styles vertical() {
        map.put(Style.vertical.key, Style.vertical.value);
        return this;
    }

    public Styles horizontal() {
        map.put(Style.horizontal.key, Style.horizontal.value);
        return this;
    }

    // POSITION

    public Styles left() {
        map.put(Style.left.key, Style.left.value);
        return this;
    }

    public Styles right() {
        map.put(Style.right.key, Style.right.value);
        return this;
    }

    public Styles top() {
        map.put(Style.top.key, Style.top.value);
        return this;
    }

    public Styles bottom() {
        map.put(Style.bottom.key, Style.bottom.value);
        return this;
    }

    public Styles center() {
        map.put(Style.center.key, Style.center.value);
        return this;
    }

    // PADDING

    /**
     * Adds default padding to the left, right, top and bottom.
     */
    public Styles padding() {
        map.put(Style.padding_left.key, Style.padding_left.value);
        map.put(Style.padding_right.key, Style.padding_right.value);
        map.put(Style.padding_top.key, Style.padding_top.value);
        map.put(Style.padding_bottom.key, Style.padding_bottom.value);
        return this;
    }

    public Styles padding(int px) {
        map.put(Style.padding_left.key, "" + (byte) px);
        map.put(Style.padding_right.key, "" + (byte) px);
        map.put(Style.padding_top.key, "" + (byte) px);
        map.put(Style.padding_bottom.key, "" + (byte) px);
        return this;
    }

    public Styles paddingLeft() {
        map.put(Style.padding_left.key, Style.padding_left.value);
        return this;
    }

    public Styles paddingLeft(int px) {
        map.put(Style.padding_left.key, "" + (byte) px);
        return this;
    }

    public Styles paddingRight() {
        map.put(Style.padding_right.key, Style.padding_right.value);
        return this;
    }

    public Styles paddingRight(int px) {
        map.put(Style.padding_right.key, "" + (byte) px);
        return this;
    }

    public Styles paddingTop() {
        map.put(Style.padding_top.key, Style.padding_top.value);
        return this;
    }

    public Styles paddingTop(int px) {
        map.put(Style.padding_top.key, "" + (byte) px);
        return this;
    }

    public Styles paddingBottom() {
        map.put(Style.padding_bottom.key, Style.padding_bottom.value);
        return this;
    }

    public Styles paddingBottom(int px) {
        map.put(Style.padding_bottom.key, "" + (byte) px);
        return this;
    }

    /**
     * Deletes all the padding.
     */
    public Styles delPadding() {
        map.remove(Style.padding_left.key);
        map.remove(Style.padding_right.key);
        map.remove(Style.padding_top.key);
        map.remove(Style.padding_bottom.key);
        return this;
    }


    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> stylesMap) {
        this.map = stylesMap;
    }

    // JAVA AWT COMPONENT STUFF

    /**
     * @see #width(int)
     */
    public Styles widthFull() {
        width(100);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null or its parent is null.
     */
    public Styles width(int widthPercent) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(component.getParent());
        updateSizes(component.getParent(), component, widthPercent, component.getHeight());
        return this;
    }

    /**
     * @see #height(int)
     */
    public Styles heightFull() {
        height(100);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null or its parent is null.
     */
    public Styles height(int heightPercent) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(component.getParent());
        updateSizes(component.getParent(), component, component.getWidth(), heightPercent);
        return this;
    }

    private void updateSizes(Component parent, Component target, int widthPercent, int heightPercent) {
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

        // Update container sizes
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMinimumSize(size);
        target.setMaximumSize(size);
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles background(Color color) {
        Objects.requireNonNull(component);
        component.setBackground(color);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles foreground(Color color) {
        Objects.requireNonNull(component);
        component.setForeground(color);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles enable() {
        Objects.requireNonNull(component);
        component.setEnabled(true);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles disable() {
        Objects.requireNonNull(component);
        component.setEnabled(false);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles show() {
        Objects.requireNonNull(component);
        component.setVisible(true);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     * @throws NullPointerException if {@link #component} null.
     */
    public Styles hide() {
        Objects.requireNonNull(component);
        component.setVisible(false);
        return this;
    }
}
