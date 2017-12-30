/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel.debug;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIHUDPanel;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.Fonts;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

public abstract class AbstractDebugPanel extends UIHUDPanel {

    protected final Minecraft client = Minecraft.getMinecraft();
    boolean autoSize = true;
    int baseWidth;
    int baseHeight;
    int autoWidth;
    int autoHeight;

    AbstractDebugPanel(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height);
        this.baseWidth = width;
        this.baseHeight = height;
    }

    final void drawProperty(final String key, final String value, final int x, final int y) {
        final Text text = Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
        this.drawText0(text, x, y, true, true, 2);
    }

    final void drawText(final Text text, final int x, final int y) {
        this.drawText(text, x, y, true, true);
    }

    final void drawText(final Text text, final int x, final int y, final boolean adjustAutoHeight, final boolean adjustAutoWidth) {
        this.drawText0(text, x, y, adjustAutoHeight, adjustAutoWidth, 0);
    }

    @SuppressWarnings("deprecation")
    private void drawText0(final Text text, final int x, int y, final boolean aah, final boolean aaw, final int offset) {
        if (y == 0) {  // Pre-set start value.
            y+= +5; // Specify the first starting spot within the panel
            this.autoHeight = y; //Set this value for proceeding checks again autoHeight.
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

    @Nullable
    protected final Entity getView() {
        final Entity view = this.client.getRenderViewEntity();
        if (view == null || this.client.player == null || this.client.player.world == null || !this.client.gameSettings.showDebugInfo) {
            return null;
        }
        return view;
    }

    final int autoSizeWidth() {
        return this.autoWidth + 4;
    }
}
