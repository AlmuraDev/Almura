/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud.debug;

import com.almuradev.almura.client.gui.component.hud.UIHUDPanel;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.google.common.collect.ImmutableMap;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

public abstract class AbstractDebugPanel extends UIHUDPanel {

    private static final TextTemplate KEY_VALUE_TEXT_TEMPLATE = TextTemplate.of(
            TextTemplate.arg("key").color(TextColors.WHITE), ": ",
            TextTemplate.arg("value").color(TextColors.GRAY)
    );
    protected final Minecraft client = Minecraft.getMinecraft();
    protected final MalisisFont font = MalisisFont.minecraftFont;
    private final FontOptions fontOptions = FontOptionsConstants.FRO_COLOR_WHITE;
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

    void drawText(GuiRenderer renderer, Text text, int x, int y) {
        this.drawText(renderer, text, x, y, true, true);
    }

    @SuppressWarnings("deprecation")
    void drawText(GuiRenderer renderer, Text text, int x, int y, boolean adjustAutoHeight, boolean adjustAutoWidth) {
        renderer.drawText(this.font, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, this.fontOptions);

        // AutoSize properties
        if (adjustAutoHeight) {
            this.autoHeight += (int) this.font.getStringHeight(this.fontOptions) + 4;
        }
        if (adjustAutoWidth) {
            this.autoWidth = Math.max(this.client.fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text)) + x, this.autoWidth);
        }
    }

    void drawProperty(GuiRenderer renderer, String key, String value, int x, int y) {
        this.drawProperty(renderer, key, value, x, y, true, true);
    }

    @SuppressWarnings("deprecation")
    private void drawProperty(GuiRenderer renderer, String key, String value, int x, int y, boolean adjustAutoHeight, boolean adjustAutoWidth) {
        final Text text = KEY_VALUE_TEXT_TEMPLATE.apply(ImmutableMap.of("key", Text.of(key), "value", Text.of(value))).build();
        renderer.drawText(this.font, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, this.fontOptions);

        // AutoSize properties
        if (adjustAutoHeight) {
            this.autoHeight += (int) this.font.getStringHeight(this.fontOptions) + 4;
        }
        if (adjustAutoWidth) {
            this.autoWidth = Math.max(this.client.fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text).trim()) + x + 2,
                    this.autoWidth);
        }
    }

    @Nullable
    protected Entity getView() {
        final Entity view = this.client.getRenderViewEntity();
        if (view == null || this.client.player == null || this.client.player.world == null || !this.client.gameSettings.showDebugInfo) {
            return null;
        }
        return view;
    }

    public boolean getAutoSize() {
        return this.autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    protected int getAutoSizeWidth() {
        return this.autoWidth + 4;
    }

    protected int getAutoSizeHeight() {
        return this.autoHeight;
    }
}
