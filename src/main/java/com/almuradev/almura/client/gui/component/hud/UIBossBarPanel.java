/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIBossBarPanel extends UIHUDPanel {

    private static final Map<BossBarColor, org.spongepowered.api.util.Color> mapBossBarColorAdapter = new HashMap<>();
    private static final int barHeight = 5;

    static {
        mapBossBarColorAdapter.put(BossBarColors.BLUE, org.spongepowered.api.util.Color.ofRgb(0, 183, 236));
        mapBossBarColorAdapter.put(BossBarColors.GREEN, org.spongepowered.api.util.Color.ofRgb(29, 236, 0));
        mapBossBarColorAdapter.put(BossBarColors.PINK, org.spongepowered.api.util.Color.ofRgb(236, 0, 184));
        mapBossBarColorAdapter.put(BossBarColors.PURPLE, org.spongepowered.api.util.Color.ofRgb(123, 0, 236));
        mapBossBarColorAdapter.put(BossBarColors.RED, org.spongepowered.api.util.Color.ofRgb(236, 53, 0));
        mapBossBarColorAdapter.put(BossBarColors.WHITE, org.spongepowered.api.util.Color.ofRgb(236, 236, 236));
        mapBossBarColorAdapter.put(BossBarColors.YELLOW, org.spongepowered.api.util.Color.ofRgb(233, 236, 0));
    }

    public UIBossBarPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        int contentHeight = 0;

        final List<BossInfoClient> bossInfoClients = new ArrayList<>(((IMixinGuiBossOverlay) Minecraft.getMinecraft().ingameGUI.getBossOverlay())
                .getBossInfo().values());

        final int segmentHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 9;
        int startY = 6;
        for (BossInfoClient bossInfoClient : bossInfoClients) {
            this.drawBossSegment(renderer, (BossBar) bossInfoClient, startY);
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
        final int textX = (this.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(text)) / 2;
        renderer.drawText(text, textX, startY, zIndex);

        // Increase the starting Y position
        startY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1;

        // Figure out how we're drawing it
        if (bossBar.getOverlay() == BossBarOverlays.NOTCHED_6) { // NOTCHED_6
            this.drawNotched(renderer, bossBar, startY, contentWidth, 6);
        } else if (bossBar.getOverlay() == BossBarOverlays.NOTCHED_10) { // NOTCHED_10
            this.drawNotched(renderer, bossBar, startY, contentWidth, 10);
        } else if (bossBar.getOverlay() == BossBarOverlays.NOTCHED_12) { // NOTCHED_12
            this.drawNotched(renderer, bossBar, startY, contentWidth, 12);
        } else if (bossBar.getOverlay() == BossBarOverlays.NOTCHED_20) { // NOTCHED_20
            this.drawNotched(renderer, bossBar, startY, contentWidth, 20);
        } else { // PROGRESS
            this.drawNotched(renderer, bossBar, startY, contentWidth, 10);
//            this.drawProgress(renderer, bossBar, startY, contentWidth);
        }
    }

    private void drawProgress(GuiRenderer renderer, BossBar bossBar, int startY, int contentWidth) {
        renderer.drawRectangle(6, startY, this.zIndex, this.width - 12, barHeight, 0, 255);
        renderer.drawRectangle(7, startY + 1, this.zIndex, contentWidth, barHeight - 2, getColorFromBossBarColor(bossBar.getColor())
                        .getRgb(), 255);
    }

    private void drawNotched(GuiRenderer renderer, BossBar bossBar, int startY, int contentWidth, int notches) {
        this.drawProgress(renderer, bossBar, startY, contentWidth);

        final int[] parts = splitIntoParts(this.width - 14, notches);
        int notchX = 6;
        for (int part : parts) {
            notchX += part;
            if (notchX - 6 < contentWidth) {
                renderer.drawRectangle(notchX, startY + 1, this.zIndex, 1, 3, 0, 100);
            }
        }
    }

    private static int[] splitIntoParts(int whole, int parts) {
        final int[] arr = new int[parts];
        int remain = whole;
        int partsLeft = parts;
        for (int i = 0; partsLeft > 0; i++) {
            int size = (remain + partsLeft - 1) / partsLeft;
            arr[i] = size;
            remain -= size;
            partsLeft--;
        }
        return arr;
    }

    private static org.spongepowered.api.util.Color getColorFromBossBarColor(BossBarColor bossBarColor) {
        return mapBossBarColorAdapter.getOrDefault(bossBarColor, TextColors.AQUA.getColor());
    }
}
