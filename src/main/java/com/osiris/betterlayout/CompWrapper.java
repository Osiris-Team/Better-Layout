/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Wrapper around a {@link Style}s map that
 * provides util methods for setting and retrieving stuff.
 */
public class CompWrapper {
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
    public DebugInfo info;

    public Component component;

    public CompWrapper() {
        this(null, null);
    }

    public CompWrapper(Component component) {
        this(component, null);
    }

    public CompWrapper(Component component, Map<String, String> map) {
        this.component = component;
        if (map == null) this.map = new HashMap<>();
        else this.map = map;
    }

    // ALIGNMENT

    public CompWrapper vertical() {
        map.put(Style.vertical.key, Style.vertical.value);
        return this;
    }

    public CompWrapper horizontal() {
        map.put(Style.horizontal.key, Style.horizontal.value);
        return this;
    }

    // POSITION

    public CompWrapper left() {
        map.put(Style.left.key, Style.left.value);
        return this;
    }

    public CompWrapper right() {
        map.put(Style.right.key, Style.right.value);
        return this;
    }

    public CompWrapper top() {
        map.put(Style.top.key, Style.top.value);
        return this;
    }

    public CompWrapper bottom() {
        map.put(Style.bottom.key, Style.bottom.value);
        return this;
    }

    public CompWrapper center() {
        map.put(Style.center.key, Style.center.value);
        return this;
    }

    // PADDING

    /**
     * Adds default padding to the left, right, top and bottom.
     */
    public CompWrapper padding() {
        map.put(Style.padding_left.key, Style.padding_left.value);
        map.put(Style.padding_right.key, Style.padding_right.value);
        map.put(Style.padding_top.key, Style.padding_top.value);
        map.put(Style.padding_bottom.key, Style.padding_bottom.value);
        return this;
    }

    public CompWrapper padding(int px) {
        map.put(Style.padding_left.key, "" + (byte) px);
        map.put(Style.padding_right.key, "" + (byte) px);
        map.put(Style.padding_top.key, "" + (byte) px);
        map.put(Style.padding_bottom.key, "" + (byte) px);
        return this;
    }

    public CompWrapper paddingLeft() {
        map.put(Style.padding_left.key, Style.padding_left.value);
        return this;
    }

    public CompWrapper paddingLeft(int px) {
        map.put(Style.padding_left.key, "" + (byte) px);
        return this;
    }

    public CompWrapper paddingRight() {
        map.put(Style.padding_right.key, Style.padding_right.value);
        return this;
    }

    public CompWrapper paddingRight(int px) {
        map.put(Style.padding_right.key, "" + (byte) px);
        return this;
    }

    public CompWrapper paddingTop() {
        map.put(Style.padding_top.key, Style.padding_top.value);
        return this;
    }

    public CompWrapper paddingTop(int px) {
        map.put(Style.padding_top.key, "" + (byte) px);
        return this;
    }

    public CompWrapper paddingBottom() {
        map.put(Style.padding_bottom.key, Style.padding_bottom.value);
        return this;
    }

    public CompWrapper paddingBottom(int px) {
        map.put(Style.padding_bottom.key, "" + (byte) px);
        return this;
    }

    /**
     * Deletes all the padding.
     */
    public CompWrapper delPadding() {
        map.remove(Style.padding_left.key);
        map.remove(Style.padding_right.key);
        map.remove(Style.padding_top.key);
        map.remove(Style.padding_bottom.key);
        return this;
    }

    // JAVA AWT COMPONENT STUFF

    /**
     * @see #width(int)
     */
    public CompWrapper widthFull() {
        width(100);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null or its parent is null.
     */
    public CompWrapper width(int widthPercent) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(component.getParent());
        updateWidth(component.getParent(), component, widthPercent);
        return this;
    }

    /**
     * @see #height(int)
     */
    public CompWrapper heightFull() {
        height(100);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null or its parent is null.
     */
    public CompWrapper height(int heightPercent) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(component.getParent());
        updateHeight(component.getParent(), component, heightPercent);
        return this;
    }

    private void updateWidth(Component parent, Component target, int widthPercent) {
        int parentWidth; // If no parent provided use the screen dimensions
        if (parent != null) parentWidth = parent.getWidth();
        else parentWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        Dimension size = new Dimension(parentWidth / 100 * widthPercent, target.getHeight());
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMinimumSize(size);
        target.setMaximumSize(size);
    }

    private void updateHeight(Component parent, Component target, int heightPercent) {
        int parentHeight; // If no parent provided use the screen dimensions
        if (parent != null) parentHeight = parent.getHeight();
        else parentHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Dimension size = new Dimension(target.getWidth(), parentHeight / 100 * heightPercent);
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
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper background(Color color) {
        Objects.requireNonNull(component);
        component.setBackground(color);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper foreground(Color color) {
        Objects.requireNonNull(component);
        component.setForeground(color);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper enable() {
        Objects.requireNonNull(component);
        component.setEnabled(true);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper disable() {
        Objects.requireNonNull(component);
        component.setEnabled(false);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper show() {
        Objects.requireNonNull(component);
        component.setVisible(true);
        return this;
    }

    /**
     * Relies on the component having a parent, so make
     * sure to call this after adding it to a {@link Container}. <p>
     * This invalidates the container and thus to see changes in the UI
     * make sure to call this within {@link BLayout#access(Runnable)} or execute {@link Component#revalidate()} manually.
     *
     * @throws NullPointerException if {@link #component} null.
     */
    public CompWrapper hide() {
        Objects.requireNonNull(component);
        component.setVisible(false);
        return this;
    }

    public CompWrapper onClick(Consumer<MouseEvent> action){
        component.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.accept(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return this;
    }

    public CompWrapper onMouseEnter(Consumer<MouseEvent> action){
        component.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                action.accept(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return this;
    }

    public CompWrapper onMouseExit(Consumer<MouseEvent> action){
        component.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {
                action.accept(e);
            }
        });
        return this;
    }

    public CompWrapper onScroll(Consumer<MouseWheelEvent> action){
        component.addMouseWheelListener(action::accept);
        return this;
    }

    public CompWrapper onFocusGain(Consumer<FocusEvent> action){
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                action.accept(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        return this;
    }

    public CompWrapper onFocusLoss(Consumer<FocusEvent> action){
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                action.accept(e);
            }
        });
        return this;
    }

    public CompWrapper onKeyType(Consumer<KeyEvent> action){
        component.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                action.accept(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        return this;
    }

    public CompWrapper onInputChange(Consumer<InputMethodEvent> action){
        component.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                action.accept(event);
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {

            }
        });
        return this;
    }

    public CompWrapper onCaretChange(Consumer<InputMethodEvent> action){
        component.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                action.accept(event);
            }
        });
        return this;
    }

    public CompWrapper onResize(Consumer<ComponentEvent> action){
        component.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                action.accept(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        return  this;
    }

    public CompWrapper onMoved(Consumer<ComponentEvent> action){
        component.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {
                action.accept(e);
            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        return  this;
    }

    public CompWrapper onShow(Consumer<ComponentEvent> action){
        component.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {
                action.accept(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        return  this;
    }

    public CompWrapper onHide(Consumer<ComponentEvent> action){
        component.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {
                action.accept(e);
            }
        });
        return  this;
    }

    public CompWrapper onPropertyChange(Consumer<PropertyChangeEvent> action){
        component.addPropertyChangeListener(action::accept);
        return  this;
    }


}
