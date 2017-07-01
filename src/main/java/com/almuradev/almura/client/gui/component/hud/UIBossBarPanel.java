/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.asm.mixin.interfaces.IMixinBossBarColor;
import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.util.MathUtil;
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
            this.drawBossSegment(renderer, (BossBar) bar, startY);
            startY += segmentHeight;
            contentHeight += segmentHeight;
        }

        this.height = contentHeight + 8;
    }

    @SuppressWarnings("deprecation")
    private void drawBossSegment(GuiRenderer renderer, BossBar bossBar, int startY) {
        // Get the width of the bar we will fill with color
        final int contentWidth = (int) MathUtil.convertToRange(bossBar.getPercent(), 0f, 1f, 0, this.width - 14);

        // Draw the text
        final String text = TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, bossBar.getName()));
        final int textX = (this.width - this.client.fontRenderer.getStringWidth(text)) / 2;
        renderer.drawText(text, textX, startY, this.zIndex);

        // Increase the starting Y position
        startY += this.client.fontRenderer.FONT_HEIGHT + 1;

        // Figure out how we're drawing it
        final BossBarOverlay overlay = bossBar.getOverlay();
        if (overlay == BossBarOverlays.NOTCHED_6) {
            this.drawNotched(renderer, bossBar, startY, contentWidth, 6);
        } else if (overlay == BossBarOverlays.NOTCHED_10) {
            this.drawNotched(renderer, bossBar, startY, contentWidth, 10);
        } else if (overlay == BossBarOverlays.NOTCHED_12) {
            this.drawNotched(renderer, bossBar, startY, contentWidth, 12);
        } else if (overlay == BossBarOverlays.NOTCHED_20) {
            this.drawNotched(renderer, bossBar, startY, contentWidth, 20);
        } else if (overlay == BossBarOverlays.PROGRESS) {
            this.drawProgress(renderer, bossBar, startY, contentWidth);
        }
    }

    private void drawProgress(GuiRenderer renderer, BossBar bossBar, int startY, int contentWidth) {
        renderer.drawRectangle(6, startY, this.zIndex, this.width - 12, BAR_HEIGHT, 0, 255);
        final int rgb = ((IMixinBossBarColor) bossBar.getColor()).getAlmuraColor().getRgb();
        renderer.drawRectangle(7, startY + 1, this.zIndex, contentWidth, BAR_HEIGHT - 2, rgb, 255);
    }

    private void drawNotched(GuiRenderer renderer, BossBar bossBar, int startY, int contentWidth, int notches) {
        this.drawProgress(renderer, bossBar, startY, contentWidth);

        int notchX = 6;
        final int notchWidth = (this.width - 14) / notches;
        for (int i = 0; i < notches; i++) {
            notchX += notchWidth;
            if (notchX - 6 < contentWidth) {
                renderer.drawRectangle(notchX, startY + 1, this.zIndex, 1, 3, 0, 100);
            }
        }
    }
}
