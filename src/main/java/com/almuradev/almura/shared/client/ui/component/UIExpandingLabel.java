/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.util.bbcode.BBString;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIExpandingLabel extends UILabel {

    public UIExpandingLabel(MalisisGui gui, String text, boolean multiLine) {
        super(gui, text, multiLine);
    }

    public UIExpandingLabel(MalisisGui gui, BBString text) {
        super(gui, text);
    }

    public UIExpandingLabel(MalisisGui gui, String text) {
        super(gui, text);
    }

    public UIExpandingLabel(MalisisGui gui, boolean multiLine) {
        super(gui, multiLine);
    }

    @Override
    public int getContentWidth() {
        return this.textWidth;
    }

    @Override
    public UILabel setText(String text) {
        if (text != null && text.equals(this.text)) {
            return this;
        }

        this.text = text;
        this.bbText = null;
        calculateSize();
        if (this.multiLine) {
            buildLines();
        }

        return this;
    }

    @Override
    protected void calculateSize() {
        this.textWidth = 0;
        int lines = 0;
        for (String line : this.text.split("\\n")) {
            int lineWidth = (int) this.font.getStringWidth(line, this.fontOptions) + 5;
            if (this.textWidth < lineWidth) {
                this.textWidth = lineWidth;
            }
            lines++;
        }

        // May be a saner way of handling but it appears working.
        // We take the line height based on the font/fontoptions and add it to the fontscale (as a means to determine line spacing, probably wrong
        // but it works for the moment) and then multiply by the total number of lines and tada!
        this.textHeight = Math.round(lines * (this.font.getStringHeight(this.fontOptions) + this.fontOptions.getFontScale()));

        setSize(this.textWidth, this.textHeight);
        setPosition(this.x, this.y, this.anchor);
    }
}
