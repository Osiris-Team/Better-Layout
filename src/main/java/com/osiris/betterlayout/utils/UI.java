/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.betterlayout.utils;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class UI {

    public static void refresh(Component comp) {
        SwingUtilities.invokeLater(() -> {
            comp.repaint();
            comp.revalidate();
            comp.resize(comp.getPreferredSize());
        });
    }

    public static void validateDown(Component comp) {
        synchronized (comp.getTreeLock()) {
            if (!(comp instanceof Container)) comp = comp.getParent();
            if (comp == null) return;
            Container container = (Container) comp;
            container.validate();
            for (Component c : container.getComponents()) {
                if (c instanceof Container)
                    validateDown(c);
            }
        }
    }

    /**
     * @see #validateAllUp(Component, boolean)
     */
    public static void validateAllUp(Component comp) {
        validateAllUp(comp, false);
    }

    /**
     * Does {@link Component#validate()} for all containers. However, instead going from parent to child,
     * it goes from child to parent, to ensure
     * child sizes are correct before doing the parent sizes. <p>
     * <p>
     * It finds all the child containers that are the furthest away
     * from this container and moves up (validates each) until it reaches this container.
     * Then validates this and continues going up and validating each until reaching the root container.<p>
     * <p>
     * If onlyUp is true then it skips the search for the furthest child
     * containers and validates only this container and the containers above it, until the root container. <p>
     * <p>
     * Note that only validates if needed aka the container is invalid or a child component of
     * the container is invalid.
     */
    public static void validateAllUp(Component comp, boolean onlyUp) {
        synchronized (comp.getTreeLock()) {
            if (!(comp instanceof Container))
                comp = comp.getParent();
            if (comp == null) return;
            Container container = (Container) comp;
            if (!onlyUp) { // Validate all child containers up to this container
                java.util.List<Container> containersFurthestAway = new ArrayList<>();
                for (Component c : container.getComponents()) {
                    if (c instanceof Container)
                        containersFurthestAway.add(findFurthestChildContainer((Container) c));
                }
                for (Container childContainer : containersFurthestAway) {
                    Container parent = childContainer;
                    while (parent != container) {
                        //System.err.println("VALIDATINGGG: "+parent.getClass().getSimpleName()+"/"+Integer.toHexString(parent.hashCode()));
                        parent.validate();
                        parent = parent.getParent();
                    }
                }
            }

            Container parent = container; // Validate up till root
            while (parent != null) {
                parent.validate();
                parent = parent.getParent();
            }
        }
    }

    private static Container findFurthestChildContainer(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof Container)
                return findFurthestChildContainer((Container) component);
        }
        return container;
    }

    /**
     * @see #validateAllUp(Component, boolean)
     */
    public static void revalidateAllUp(Component comp) {
        revalidateAllUp(comp, false);
    }

    /**
     * Does {@link Component#revalidate()} for all containers. However, instead going from parent to child,
     * it goes from child to parent, to ensure
     * child sizes are correct before doing the parent sizes. <p>
     * <p>
     * It finds all the child containers that are the furthest away
     * from this container and moves up (validates each) until it reaches this container.
     * Then validates this and continues going up and validating each until reaching the root container.<p>
     * <p>
     * If onlyUp is true then it skips the search for the furthest child
     * containers and validates only this container and the containers above it, until the root container. <p>
     * <p>
     * Note that only validates if needed aka the container is invalid or a child component of
     * the container is invalid.
     */
    public static void revalidateAllUp(Component comp, boolean onlyUp) {
        synchronized (comp.getTreeLock()) {
            if (!(comp instanceof Container))
                comp = comp.getParent();
            if (comp == null) return;
            Container container = (Container) comp;
            if (!onlyUp) { // Validate all child containers up to this container
                java.util.List<Container> containersFurthestAway = new ArrayList<>();
                for (Component c : container.getComponents()) {
                    if (c instanceof Container)
                        containersFurthestAway.add(findFurthestChildContainer((Container) c));
                }
                for (Container childContainer : containersFurthestAway) {
                    Container parent = childContainer;
                    while (parent != container) {
                        //System.err.println("VALIDATINGGG: "+parent.getClass().getSimpleName()+"/"+Integer.toHexString(parent.hashCode()));
                        parent.invalidate();
                        parent.validate();
                        parent = parent.getParent();
                    }
                }
            }

            Container parent = container; // Validate up till root
            while (parent != null) {
                parent.invalidate();
                parent.validate();
                parent = parent.getParent();
            }
        }
    }


}
