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

    public UILine(MalisisGui gui, int width) {
        this(gui, width, 1);
    }

    public UILine(MalisisGui gui, int width, int thickness) {
        super(gui, width, thickness);
        this.setColor(FontColors.WHITE);
        this.setBackgroundAlpha(185);
    }
}
