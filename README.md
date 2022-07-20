# Better-Layout
Probably the best and most modern layout for Java AWT/Swing

![](assets/example.png)

```java
// ... window code not shown
BLayout rootLayout = new BLayout();
this.setContentPane(rootLayout);
rootLayout.isDebug = true;
rootLayout.access(() -> { // No need to call revalidate on any component inside here
    BLayout lyTitle = new BLayout(true);
    rootLayout.addV(lyTitle);
    JLabel titleAutoPlug = new JLabel(), titleTray = new JLabel();

    titleAutoPlug.setText("My title");
    titleAutoPlug.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
    lyTitle.addH(titleAutoPlug).paddingLeft();

    titleTray.setText("| my subtile");
    titleTray.putClientProperty("FlatLaf.style", "font: 200% $light.font");
    lyTitle.addH(titleTray).paddingLeft();
    
    // Adds this component to the next line/row.
    // Basicaly the same as "\n" in a string.    
    rootLayout.addV(new JLabel("VERTICAL"));
    rootLayout.addH(new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"));
    rootLayout.addV(new JLabel("VERTICAL"));
    rootLayout.addH(new JLabel("HORIZONTAL"));
    rootLayout.addV(new BLayout(rootLayout, 30, 30));
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("hello!", new BLayout());
    rootLayout.addV(tabbedPane);
});
```

### Features

- Compatible with all Java AWT/Swing components and containers.
- Styles per component, supports:
  - Horizontal and Vertical positioning.
  - Right, left, top and bottom padding.
- Easy validation/revalidation of container and child containers via `access()` method.