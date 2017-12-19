/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.asm.mixin.interfaces.IMixinBossBarColor;
import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;

public class UIBossBarPanel extends UIHUDPanel {

    private static final int BAR_HEIGHT = 5;
    private final Minecraft client = Minecraft.getMinecraft();

    public UIBossBarPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        int contentHeight = 0;

        final Collection<BossInfoClient> bars = ((IMixinGuiBossOverlay) this.client.ingameGUI.getBossOverlay()).getBossInfo().values();

        final int segmentHeight = this.client.fontRenderer.FONT_HEIGHT + 9;
        int startY = 6;
        for (BossInfoClient bar : bars) {
            this.drawBossSegment((BossBar) bar, startY);
            startY += segmentHeight;
            contentHeight += segmentHeight;
        }

        this.height = contentHeight + 8;
    }

    @SuppressWarnings("deprecation")
    private void drawBossSegment(final BossBar bar, int startY) {
        // Get the width of the bar we will fill with color
        final int contentWidth = (int) MathUtil.convertToRange(bar.getPercent(), 0f, 1f, 0, this.width - 14);

        // Draw the text
        final String text = TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, bar.getName()));
        final int textX = (this.width - this.client.fontRenderer.getStringWidth(text)) / 2;
        this.renderer.drawText(text, textX, startY, this.zIndex);

        // Increase the starting Y position
        startY += this.client.fontRenderer.FONT_HEIGHT + 1;

        // Figure out how we're drawing it
        final BossBarOverlay overlay = bar.getOverlay();
        if (overlay == BossBarOverlays.NOTCHED_6) {
            this.drawNotched(bar, startY, contentWidth, 6);
        } else if (overlay == BossBarOverlays.NOTCHED_10) {
            this.drawNotched(bar, startY, contentWidth, 10);
        } else if (overlay == BossBarOverlays.NOTCHED_12) {
            this.drawNotched(bar, startY, contentWidth, 12);
        } else if (overlay == BossBarOverlays.NOTCHED_20) {
            this.drawNotched(bar, startY, contentWidth, 20);
        } else if (overlay == BossBarOverlays.PROGRESS) {
            this.drawProgress(bar, startY, contentWidth);
        }
    }

    private void drawProgress(final BossBar bar, final int startY, final int contentWidth) {
        this.renderer.drawRectangle(6, startY, this.zIndex, this.width - 12, BAR_HEIGHT, 0, 255);
        final int rgb = ((IMixinBossBarColor) bar.getColor()).getAlmuraColor().getRgb();
        this.renderer.drawRectangle(7, startY + 1, this.zIndex, contentWidth, BAR_HEIGHT - 2, rgb, 255);
    }

    private void drawNotched(final BossBar bar, final int startY, final int contentWidth,final  int notches) {
        this.drawProgress(bar, startY, contentWidth);

        int notchX = 6;
        final int notchWidth = (this.width - 14) / notches;
        for (int i = 0; i < notches; i++) {
            notchX += notchWidth;
            if (notchX - 6 < contentWidth) {
                this.renderer.drawRectangle(notchX, startY + 1, this.zIndex, 1, 3, 0, 100);
            }
        }
    }
}
