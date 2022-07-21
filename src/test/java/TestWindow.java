/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

import com.formdev.flatlaf.FlatLightLaf;
import com.osiris.betterlayout.BLayout;
import com.osiris.betterlayout.utils.UIDebugWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;

public class TestWindow extends JFrame {
    /**
     * There should always be only one instance of {@link TestWindow}.
     */
    public static TestWindow GET = null;

    public TestWindow() throws Exception {
        if (GET != null) return;
        GET = this;
        initTheme();
        initUI();
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F12)
                    new UIDebugWindow(GET);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

    }

    public void close() {
        this.dispose();
    }

    public void initTheme() {
        if (!FlatLightLaf.setup()) throw new RuntimeException("Returned false!");
    }

    private void initUI() {
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setName("My-Window");
        this.setTitle("My-Window");
        this.setUndecorated(true);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width, screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int width = (int) (screenWidth / 1.5), height = screenHeight / 2;
        this.setShape(new RoundRectangle2D.Double(0, 0, width, height, 20, 20));
        this.setLocation((screenWidth / 2) - (width / 2), (screenHeight / 2) - (height / 2)); // Position frame in mid of screen
        this.setSize(width, height);
        this.setVisible(true);

        BLayout rootLayout = new BLayout();
        this.setContentPane(rootLayout);
        rootLayout.isDebug = true;
        rootLayout.access(() -> { // No need to call revalidate on any component inside here
            BLayout lyTitle = new BLayout(rootLayout, true);
            rootLayout.addV(lyTitle);
            JLabel titleAutoPlug = new JLabel(), titleTray = new JLabel();

            titleAutoPlug.setText("My title");
            titleAutoPlug.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
            lyTitle.addH(titleAutoPlug).paddingLeft();

            titleTray.setText("| my subtile");
            titleTray.putClientProperty("FlatLaf.style", "font: 200% $light.font");
            lyTitle.addH(titleTray).paddingLeft();

            rootLayout.addV(new JLabel("VERTICAL"));
            rootLayout.addH(new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"));
            rootLayout.addV(new JLabel("VERTICAL"));
            rootLayout.addH(new JLabel("HORIZONTAL"));
            rootLayout.addV(new BLayout()).widthFull().height(10).background(Color.red);
            new BLayout(rootLayout, 30, 10)
                    .addV(new JLabel("Lorem ipsum dolor sit amet! "),
                            new JLabel("Lorem ipsum dolor sit amet! "),
                            new JLabel("Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet"),
                            new JLabel("Lorem ipsum dolor sit amet! "),
                            new JLabel("Lorem ipsum dolor sit amet! "))
                    .makeScrollable(); // This adds the scroll layout to the rootLayout
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("hello!", new BLayout(tabbedPane, 100, 10));
            rootLayout.addV(tabbedPane);
        });
    }
}
