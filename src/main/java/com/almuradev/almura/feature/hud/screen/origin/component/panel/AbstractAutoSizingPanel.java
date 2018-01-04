/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.Fonts;
import net.malisis.core.client.gui.MalisisGui;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

public abstract class AbstractAutoSizingPanel extends AbstractPanel {
    private final int initialTextOffsetY;
    protected int baseWidth;
    protected int baseHeight;
    protected boolean autoSize = true;
    protected int autoWidth;
    protected int autoHeight;

    protected AbstractAutoSizingPanel(final MalisisGui gui, final int width, final int height) {
        this(gui, width, height, 0);
    }

    protected AbstractAutoSizingPanel(final MalisisGui gui, final int width, final int height, final int initialTextOffsetY) {
        super(gui, width, height);
        this.baseWidth = width;
        this.baseHeight = height;
        this.initialTextOffsetY = initialTextOffsetY;
    }

    protected void autoSize() {
        if (this.autoSize) {
            this.setSize(Math.max(this.autoSizeWidth(), this.baseWidth), Math.max(this.autoHeight, this.baseHeight));
            this.autoHeight = 0;
            this.autoWidth = 0;
        }
    }

    protected final int autoSizeWidth() {
        return this.autoWidth + 4;
    }

    protected final void drawProperty(final String key, final String value, final int x) {
        this.drawProperty(key, value, x, this.autoHeight);
    }

    @Override
    protected final void drawProperty(final String key, final String value, final int x, final int y) {
        final Text text = Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
        this.drawText0(text, x, y, true, true, 2);
    }

    @Override
    protected final void drawText(final Text text, final int x, final int y) {
        this.drawText(text, x, y, true, true);
    }

    protected final void drawText(final Text text, final int x, final int y, final boolean adjustAutoHeight, final boolean adjustAutoWidth) {
        this.drawText0(text, x, y, adjustAutoHeight, adjustAutoWidth, 0);
    }

    @SuppressWarnings("deprecation")
    private void drawText0(final Text text, final int x, int y, final boolean aah, final boolean aaw, final int offset) {
        // Adjust the value of y if it is 0.
        if (this.initialTextOffsetY > 0 && y == 0) {
            y += this.initialTextOffsetY;
            this.autoHeight = y;
        }

        this.renderer.drawText(Fonts.MINECRAFT, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, FontColors.WHITE_FO);

        if (aah) {
            this.autoHeight += (int) Fonts.MINECRAFT.getStringHeight(FontColors.WHITE_FO) + 4;
        }

        if (aaw) {
            final int width = this.client.fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text).trim());
            this.autoWidth = Math.max(width + x + offset, this.autoWidth);
        }
    }
}
