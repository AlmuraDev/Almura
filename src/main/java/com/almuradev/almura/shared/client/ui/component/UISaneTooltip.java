/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.position.Position;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * A tooltip kept inside the screen.
 */
public class UISaneTooltip extends UITooltip {

    public UISaneTooltip() {
        super();
        this.setPosition(Position.of(this::screenClampedX, this::screenClampedY));
    }

    public UISaneTooltip(String text) {
        this();
        this.setText(text);
    }

    @SuppressWarnings("deprecation")
    public UISaneTooltip(Text text) {
        this(TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
    }

    private int screenClampedX() {
        final int x = MalisisGui.MOUSE_POSITION.x() + this.getOffsetX();
        final MalisisGui gui = MalisisGui.current();
        if (gui != null && x + this.getWidth() > gui.width) {
            // Off the right side of the screen
            return gui.width - this.getWidth();
        }
        return Math.max(x, 0);
    }

    private int screenClampedY() {
        final int y = MalisisGui.MOUSE_POSITION.y() + this.getOffsetY();
        final MalisisGui gui = MalisisGui.current();
        if (gui != null && y + this.getHeight() > gui.height) {
            // Off the bottom of the screen
            return gui.height - this.getHeight();
        }
        return Math.max(y, 0);
    }
}
