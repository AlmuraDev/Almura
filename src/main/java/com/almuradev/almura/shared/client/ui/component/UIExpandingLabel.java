/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.util.bbcode.BBString;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class UIExpandingLabel extends UILabel {

    @SuppressWarnings("deprecation")
    public UIExpandingLabel(MalisisGui gui, Text text) {
        this(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
    }

    public UIExpandingLabel(MalisisGui gui, String text) {
        super(gui, text, true);
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
        this.buildLines();
        this.calculateSize();

        return this;
    }

    @Override
    protected void buildLines() {
        lines.clear();

        if (!this.text.isEmpty()) {
            this.lines.addAll(Arrays.asList(this.text.split("\\n")));
        }

        fireEvent(new ContentUpdateEvent<>(this));
    }

    @Override
    protected void calculateSize() {
        // Iterate through all lines and find the largest width
        this.textWidth = 0;
        for (String line : this.lines) {
            int lineWidth = (int) this.font.getStringWidth(line, this.fontOptions) + 5;
            if (this.textWidth < lineWidth) {
                this.textWidth = lineWidth;
            }
        }

        // May be a saner way of handling but it appears working.
        // Take the height of the text (font/fontoptions included), add the line spacing, then multiply by line count.
        this.textHeight = Math.round(this.lines.size() * (this.font.getStringHeight(this.fontOptions) + this.lineSpacing));

        setSize(this.textWidth, this.textHeight);
        setPosition(this.x, this.y, this.anchor);
    }
}
