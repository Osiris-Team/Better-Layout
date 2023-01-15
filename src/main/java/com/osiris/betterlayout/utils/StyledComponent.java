package com.osiris.betterlayout.utils;
/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

import com.osiris.betterlayout.CompWrapper;

import java.awt.*;

public class StyledComponent {
    public Component component;
    public CompWrapper compWrapper;

    public StyledComponent(Component component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return component.getClass().getSimpleName() + "@" + Integer.toHexString(component.hashCode());
    }
}
