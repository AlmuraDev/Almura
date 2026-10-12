/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.shared.client.GuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws the Almura version of vanilla's loading screen ("Loading world", "Preparing spawn area", "Saving world"), styled after
 * {@link ConnectingGui}: a bordered panel with the Almura logo, the status text and a progress bar, over the dirt
 * background.
 *
 * <p>Drawn by hand rather than with Malisis components: vanilla draws this screen while it blocks the main thread (e.g. while the
 * integrated server prepares the spawn area), so no GUI screen is open or ticking.</p>
 */
@SideOnly(Side.CLIENT)
public final class AlmuraLoadingScreen {

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 141;
    // Matches ConnectingGui's form: background alpha 215, white border at alpha 185
    private static final int PANEL_COLOR = 0xD7101010;
    private static final int BORDER_COLOR = 0xB9FFFFFF;
    private static final int SEPARATOR_COLOR = 0x80FFFFFF;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int PROGRESS_BACK_COLOR = 0xFF404040;
    private static final int PROGRESS_COLOR = 0xFF55FF55;
    private static final int LOGO_WIDTH = 60;
    private static final int LOGO_HEIGHT = 99;
    private static final int PROGRESS_WIDTH = 160;
    private static final int PROGRESS_HEIGHT = 4;
    // Gap between the status text and the progress bar
    private static final int PROGRESS_GAP = 5;
    // Offset of the separator under the logo from the top of the panel
    private static final int SEPARATOR_Y = 108;

    private AlmuraLoadingScreen() {
    }

    /**
     * @param width scaled screen width
     * @param height scaled screen height
     * @param title vanilla's main status ("Loading world", "Saving world", ...), shown when there is no detail
     * @param message vanilla's detail (e.g. "Building terrain"), shown instead of the title when present; may be empty
     * @param progress 0-100, or negative when there is no progress to show
     */
    public static void draw(final Minecraft mc, final int width, final int height, final String title, final String message,
            final int progress) {
        drawDirtBackground(mc, width, height);

        final int left = (width - PANEL_WIDTH) / 2;
        final int top = (height - PANEL_HEIGHT) / 2;
        final int right = left + PANEL_WIDTH;
        final int bottom = top + PANEL_HEIGHT;
        final int centerX = width / 2;
        final FontRenderer font = mc.fontRenderer;

        // Panel and border
        Gui.drawRect(left, top, right, bottom, PANEL_COLOR);
        Gui.drawRect(left, top, right, top + 1, BORDER_COLOR);
        Gui.drawRect(left, bottom - 1, right, bottom, BORDER_COLOR);
        Gui.drawRect(left, top, left + 1, bottom, BORDER_COLOR);
        Gui.drawRect(right - 1, top, right, bottom, BORDER_COLOR);

        // Logo
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(GuiConfig.Location.ALMURA_LOGO);
        Gui.drawModalRectWithCustomSizedTexture(centerX - LOGO_WIDTH / 2, top + 6, 0, 0, LOGO_WIDTH, LOGO_HEIGHT, LOGO_WIDTH, LOGO_HEIGHT);

        // Separator under the logo
        Gui.drawRect(left + 3, top + SEPARATOR_Y, right - 3, top + SEPARATOR_Y + 1, SEPARATOR_COLOR);

        // One status line: the most specific text vanilla has (the detail, e.g. "Building terrain", else the title, e.g. "Loading world" or
        // "Saving world"). Vanilla sometimes resets both to empty (e.g. loadWorld(null) just before a world loads), so fall back to a
        // generic line rather than leaving the panel without text.
        final String status = !message.isEmpty() ? message : !title.isEmpty() ? title : I18n.format("almura.menu.loading.please_wait");

        // Centre the text (and the progress bar, when shown) vertically between the separator and the bottom border
        final boolean showProgress = progress >= 0;
        final int areaTop = top + SEPARATOR_Y + 1;
        final int areaBottom = bottom - 1;
        final int textHeight = font.FONT_HEIGHT - 1;
        final int contentHeight = textHeight + (showProgress ? PROGRESS_GAP + PROGRESS_HEIGHT : 0);
        final int textY = areaTop + (areaBottom - areaTop - contentHeight) / 2;
        drawCentered(font, status, centerX, textY, TEXT_COLOR);

        // Progress bar
        if (showProgress) {
            final int barLeft = centerX - PROGRESS_WIDTH / 2;
            final int barTop = textY + textHeight + PROGRESS_GAP;
            Gui.drawRect(barLeft, barTop, barLeft + PROGRESS_WIDTH, barTop + PROGRESS_HEIGHT, PROGRESS_BACK_COLOR);
            Gui.drawRect(barLeft, barTop, barLeft + PROGRESS_WIDTH * Math.min(progress, 100) / 100, barTop + PROGRESS_HEIGHT, PROGRESS_COLOR);
        }

    }

    private static void drawCentered(final FontRenderer font, final String text, final int centerX, final int y, final int color) {
        font.drawStringWithShadow(text, centerX - font.getStringWidth(text) / 2, y, color);
    }

    /**
     * The darkened dirt background vanilla menus use with no world loaded (what ConnectingGui shows behind its form).
     */
    private static void drawDirtBackground(final Minecraft mc, final int width, final int height) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        final float scale = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(0.0D, height, 0.0D).tex(0.0D, height / scale).color(64, 64, 64, 255).endVertex();
        buffer.pos(width, height, 0.0D).tex(width / scale, height / scale).color(64, 64, 64, 255).endVertex();
        buffer.pos(width, 0.0D, 0.0D).tex(width / scale, 0.0D).color(64, 64, 64, 255).endVertex();
        buffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }
}
