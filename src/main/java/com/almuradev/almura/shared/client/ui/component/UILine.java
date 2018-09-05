/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import net.malisis.core.client.gui.MalisisGui;

public class UILine extends UIContainer<UILine> {

    public UILine(final MalisisGui gui, final int width) {
        this(gui, width, 1, false);
    }

    public UILine(final MalisisGui gui, final int size, final boolean vertical) {
        this(gui, size, 1, vertical);
    }

    public UILine(final MalisisGui gui, final int width, final int thickness) {
        this(gui, width, thickness, false);
    }

    public UILine(final MalisisGui gui, final int size, final int thickness, final boolean vertical) {
        super(gui, vertical ? thickness : size, vertical ? size : thickness);
        this.setColor(FontColors.WHITE);
        this.setBackgroundAlpha(185);
    }
}
