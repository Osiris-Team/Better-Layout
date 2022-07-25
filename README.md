# Better-Layout [![](https://jitpack.io/v/Osiris-Team/Better-Layout.svg)](https://jitpack.io/#Osiris-Team/Better-Layout)
Probably the best and most modern layout for Java AWT/Swing (Java 8 or higher required)

![](assets/img.png)

```java
// ... window code not shown
BLayout rootLayout = new BLayout();
this.setContentPane(rootLayout);
rootLayout.isDebug = true;
rootLayout.access(() -> { // No need to call revalidate on any component inside here
    BLayout lyTitle = new BLayout(rootLayout, true); // true = crop to content
    rootLayout.addV(lyTitle);
    JLabel title = new JLabel(), subtitle = new JLabel();

    title.setText("My title");
    title.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
    lyTitle.addH(titleAutoPlug).paddingLeft();

    subtitle.setText("| my subtile");
    subtitle.putClientProperty("FlatLaf.style", "font: 200% $light.font");
    lyTitle.addH(titleTray).paddingLeft();
       
    rootLayout.addV(new JLabel("VERTICAL")); // Basically the same as "\n" in a string.
    rootLayout.addH(new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"), new JLabel("HORIZONTAL"));
    rootLayout.addV(new JLabel("VERTICAL"));
    rootLayout.addH(new JLabel("HORIZONTAL"));
    new BLayout(rootLayout, 30, 10) // 30% width and 10% height
        .addV(new JLabel("Lorem ipsum dolor sit amet! "),
                new JLabel("Lorem ipsum dolor sit amet! "),
                new JLabel("Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet"),
                new JLabel("Lorem ipsum dolor sit amet! "),
                new JLabel("Lorem ipsum dolor sit amet! "))
        .makeScrollable(); // This adds the scroll layout to the rootLayout
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("hello!", new BLayout()); // 100% of window
    rootLayout.addV(tabbedPane);
});
```

### Features
- Compatible with all Java AWT/Swing components and containers.
- No need to write constraints/styles in strings, everything available through methods.

#### Design choices:
- Better-Layout tries to combine vertical & horizontal layouts
and to reduce the amount of indidual child layouts needed to create more complex layouts.
- Each child element has its own style attributes.
- Absolute positioning thorugh relative parent sizes.
- TODO: Responsive layout support.

#### Styles per component:
- Horizontal and Vertical positioning.
- Right, left, top and bottom padding.

#### Container methods for:
- Easy validation/revalidation of itself, parent and child containers via `access()` method.
- Easy addition of components vertically or horizontally via `addV()` and `addH()` methods.
- Methods to make the layout scrollable and to scroll to horizontal/vertical start/end.

