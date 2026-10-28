/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.google.common.base.MoreObjects;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIDetailsPanel extends AbstractPanel {

    // The panel's starting size (it fits itself to what it shows from the first frame) and the height of its three coordinate lines
    public static final int WIDTH = 124, HEIGHT = 37;
    private static final int COORDINATE_LINES = 3, TEXT_X = 2, TEXT_Y = 2, ICON_Y = 2, ICON_SIZE = 16, GAP = 6;
    // The widest the lines usually get, so the panel doesn't twitch wider and narrower as the numbers change
    private static final String COORDINATE_TEMPLATE = TextFormatting.GOLD + "X: " + TextFormatting.RESET + "-0000.00 ";
    private static final String STATS_TEMPLATE = TextFormatting.GOLD + "FPS: " + TextFormatting.RESET + "999  " + TextFormatting.GOLD + "TPS: "
            + TextFormatting.GREEN + "20.0";

    @Inject private static HeadUpDisplay hudData;

    private final UIImage clockImage, playerCountImage;
    private final UILabel coordsLabel, playerCountLabel;
    private final DecimalFormat df = new DecimalFormat("0.##");
    private final DecimalFormat tpsFormat = new DecimalFormat("0.0");

    public UIDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Clock
        this.clockImage = new UIImage(new ItemStack(Items.CLOCK));
        // In the top right corner, so only the first lines need room beside it and the ones below (Z, FPS / TPS) can run underneath
        this.clockImage.setPosition(-2, ICON_Y, Anchor.TOP | Anchor.RIGHT);

        // Player Count
        this.playerCountLabel = new UILabel("");
        this.playerCountLabel.setFontOptions(FontColors.WHITE_FO);
        this.playerCountImage = new UIImage(new GuiIcon(new GuiTexture(GuiConfig.Location.GENERIC_AVATAR, 16, 16), 0F, 0F, 1F, 1F));
        this.playerCountImage.setSize(ICON_SIZE, ICON_SIZE);
        this.playerCountImage.setPosition(BasicScreen.getPaddedX(this.clockImage, 4, Anchor.RIGHT), ICON_Y, Anchor.TOP | Anchor.RIGHT);

        // Coordinates
        this.coordsLabel = new UIExpandingLabel("");
        this.coordsLabel.setPosition(TEXT_X, TEXT_Y);
        this.coordsLabel.setFontOptions(FontColors.WHITE_FO);

        this.add(this.clockImage, this.playerCountLabel, this.playerCountImage, this.coordsLabel);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.client.player == null || this.client.player.world == null) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateCoordinates();
    }

    /**
     * Fits the panel to what it shows. Height: the text's lines, with the padding the three coordinate lines always had. Width: the lines
     * level with the clock and player count (top right) need room beside them, the lines below only their own width; never narrower than the
     * usual widest lines, so it doesn't twitch as numbers change. It's anchored top right, so it grows down and to the left.
     */
    private void autoSize(final List<String> lines, final boolean stats) {
        final int lineHeight = this.coordsLabel.getLineHeight();
        final int height = Math.max(HEIGHT, lines.size() * lineHeight + HEIGHT - COORDINATE_LINES * lineHeight);

        int icons = 2 + ICON_SIZE; // the clock and its margin
        if (this.playerCountImage.isVisible()) {
            icons += 4 + ICON_SIZE + 2 + this.playerCountLabel.getContentWidth();
        }
        // Lines that are level with the icons, the rest are below them
        final int besideIcons = (int) Math.ceil((ICON_Y + ICON_SIZE - TEXT_Y) / (double) lineHeight);

        int width = TEXT_X + this.textWidth(COORDINATE_TEMPLATE) + GAP + icons;
        if (stats) {
            width = Math.max(width, TEXT_X + this.textWidth(STATS_TEMPLATE) + TEXT_X);
        }
        for (int i = 0; i < lines.size(); i++) {
            width = Math.max(width, TEXT_X + this.textWidth(lines.get(i)) + (i < besideIcons ? GAP + icons : TEXT_X));
        }

        if (width != this.getWidth() || height != this.getHeight()) {
            this.setSize(width, height);
        }
    }

    private int textWidth(final String text) {
        return (int) Math.ceil(this.coordsLabel.getFont().getStringWidth(text, this.coordsLabel.getFontOptions()));
    }

    private void updateCoordinates() {
        final Entity view = MoreObjects.firstNonNull(this.client.getRenderViewEntity(), this.client.player);
        final List<String> lines = new ArrayList<>();
        lines.add(TextFormatting.GOLD + "X: " + TextFormatting.RESET + df.format(view.posX) + " ");
        lines.add(TextFormatting.GOLD + "Y: " + TextFormatting.RESET + df.format(view.posY) + " ");
        lines.add(TextFormatting.GOLD + "Z: " + TextFormatting.RESET + df.format(view.posZ));
        final boolean stats = ClientStaticAccess.configAdapter.get().general.displayPerformanceStats;
        if (stats) {
            lines.add(TextFormatting.GOLD + "FPS: " + TextFormatting.RESET + Minecraft.getDebugFPS()
                    + "  " + TextFormatting.GOLD + "TPS: " + formatTps(hudData.serverTps));
        }
        this.coordsLabel.setText(String.join("\n", lines));
        this.updatePlayerCount();
        this.autoSize(lines, stats);
    }

    /**
     * The server's TPS to one decimal, green when healthy, yellow when slowing, red when lagging ("--" until the server has sent it).
     */
    private String formatTps(final double tps) {
        if (tps < 0) {
            return TextFormatting.GRAY + "--";
        }
        final double shown = Math.min(20.0, tps);
        final TextFormatting color = shown >= 18.0 ? TextFormatting.GREEN : shown >= 15.0 ? TextFormatting.YELLOW : TextFormatting.RED;
        return color + this.tpsFormat.format(shown);
    }

    private void updatePlayerCount() {
        final boolean isOnline = !this.client.isSingleplayer() || (this.client.isSingleplayer() && this.client.getIntegratedServer().getPublic());
        if (isOnline) {
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + hudData.onlinePlayerCount + "/" + hudData.maxPlayerCount);
            // Level with the middle of the avatar
            final int labelY = ICON_Y + (ICON_SIZE - this.playerCountLabel.getLineHeight()) / 2 + 1;
            this.playerCountLabel.setPosition(BasicScreen.getPaddedX(this.playerCountImage, 2, Anchor.RIGHT), labelY, Anchor.TOP | Anchor.RIGHT);
        }
        this.playerCountImage.setVisible(isOnline);
        this.playerCountLabel.setVisible(isOnline);
    }
}
