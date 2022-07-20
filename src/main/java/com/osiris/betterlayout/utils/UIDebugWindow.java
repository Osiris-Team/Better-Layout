package com.osiris.betterlayout.utils;
/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

import com.osiris.betterlayout.BLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Objects;

public class UIDebugWindow extends JFrame {
    Component targetComponent;

    /**
     * @param targetComponent the component to analyse in this debug window.
     */
    public UIDebugWindow(Component targetComponent) {
        Objects.requireNonNull(targetComponent);
        this.targetComponent = targetComponent;
        this.setName("Debug " + targetComponent.getClass().getSimpleName());
        this.setName("Debug " + targetComponent.getClass().getSimpleName());
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width, screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int width = (screenWidth / 2), height = screenHeight / 2;
        this.setLocation((screenWidth / 2) - (width / 2), (screenHeight / 2) - (height / 2)); // Position frame in mid of screen
        this.setSize(width, height);
        this.setVisible(true);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        loadData();
    }

    private void fillTreeNodes(DefaultMutableTreeNode parentNode, CompWrapper comp) {
        DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(comp);
        parentNode.add(currentNode);
        if (comp.component instanceof Container) { // is container?
            Container container = (Container) comp.component;
            for (Component childComp : container.getComponents()) {
                fillTreeNodes(currentNode, new CompWrapper(childComp));
            }
        }
    }

    private void loadData() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.7);
        this.add(splitPane);

        JTree tree = new JTree();
        splitPane.setLeftComponent(tree);
        tree.setShowsRootHandles(true);
        tree.setEditable(false);

        BLayout lyRight = new BLayout(splitPane);
        splitPane.setRightComponent(lyRight);
        lyRight.addV(new JLabel("Double-click an item on the left, to display its details here."))
                .center();

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Inspecting contents of " + targetComponent.getClass().getSimpleName());
        fillTreeNodes(rootNode, new CompWrapper(targetComponent));

        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        tree.setModel(model);

        final Component[] beforeComponent = {null};
        final Color[] beforeColor = {null};
        tree.addTreeSelectionListener(e -> {
            // Returns the last path element of the selection.
            // This method is useful only when the selection model allows a single selection.
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();

            if (node == null) {
                // Expand all when root node is selected
                // TODO test this
                for (int i = 0; i < tree.getRowCount(); i++) {
                    tree.expandRow(i);
                }
                return; // Nothing is selected.
            }
            if (beforeComponent[0] != null) { // Restore before component
                beforeComponent[0].setBackground(beforeColor[0]);
                UIUtils.refresh(beforeComponent[0]);
                if (beforeComponent[0] instanceof BLayout)
                    ((BLayout) beforeComponent[0]).isDebug = false;
            }

            CompWrapper comp = (CompWrapper) node.getUserObject();
            Color oldBackgroundColor = comp.component.getBackground();
            comp.component.setBackground(new Color(129, 129, 129)); // Mark component, with blueish color

            if (comp.component instanceof BLayout)
                ((BLayout) comp.component).isDebug = true;

            lyRight.removeAll();
            JLabel title = new JLabel(comp.toString());
            title.putClientProperty("FlatLaf.style", "font: 100% $semibold.font");
            lyRight.addV(title);

            JTextField txtFullClassName = new JTextField(comp.getClass().getName());
            txtFullClassName.setToolTipText("The full class name/path for this object.");
            txtFullClassName.setEnabled(false);
            lyRight.addV(txtFullClassName);

            // EXTENDING/SUPER CLASSES
            Class<?> clazz = comp.component.getClass().getSuperclass();
            String extendingClasses = clazz.getSimpleName() + " -> ";
            while (!clazz.equals(Object.class)) {
                clazz = clazz.getSuperclass();
                extendingClasses += clazz.getSimpleName() + " -> ";
            }
            extendingClasses += "END";
            JTextField txtExtendingClasses = new JTextField(extendingClasses);
            txtExtendingClasses.setToolTipText("The super-classes names for this object.");
            txtExtendingClasses.setEnabled(false);
            lyRight.addV(txtExtendingClasses);

            // INTERFACES
            Class<?>[] interfaces = comp.component.getClass().getInterfaces();
            String implementingInterfaces = "";
            for (Class<?> i : interfaces) {
                implementingInterfaces += i.getSimpleName() + " ";
            }
            JTextField txtImplInterfaces = new JTextField(implementingInterfaces);
            txtImplInterfaces.setToolTipText("The implemented interfaces names for this object.");
            txtImplInterfaces.setEnabled(false);
            lyRight.addV(txtImplInterfaces);

            // DIMENSIONS
            JTextField txtDimensions = new JTextField(comp.component.getWidth() + " x " + comp.component.getHeight() + " pixels");
            txtDimensions.setToolTipText("The width x height for this object.");
            txtDimensions.setEnabled(false);
            lyRight.addV(txtDimensions);

            // LOCATION
            JTextField txtLocation = new JTextField("x: " + comp.component.getLocation().x + " y: " + comp.component.getLocation().y + " pixels");
            txtLocation.setToolTipText("The location for this object.");
            txtLocation.setEnabled(false);
            lyRight.addV(txtLocation);

            // LOCATION
            JTextField txtDetails = new JTextField("visible: " + comp.component.isVisible() + " enabled: " + comp.component.isEnabled()
                    + " valid: " + comp.component.isValid());
            txtDetails.setToolTipText("The location for this object.");
            txtDetails.setEnabled(false);
            lyRight.addV(txtDetails);

            // UPDATE STUFF
            lyRight.refresh(); // to avoid UI bug where leftover UI from before was being shown
            UIUtils.refresh(comp.component);
            beforeComponent[0] = comp.component;
            beforeColor[0] = oldBackgroundColor;
        });

        // Expand all
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
}
