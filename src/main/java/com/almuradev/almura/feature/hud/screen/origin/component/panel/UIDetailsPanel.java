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
import net.malisis.core.client.gui.element.Padding;
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
    private static final int COORDINATE_LINES = 3, TEXT_X = 2, TEXT_Y = 2, ICON_Y = 2, ICON_SIZE = 16, GAP = 6, DESCENDER = 2;
    // Right margin of the clock and the player count, and the gap between the time and the clock
    private static final int RIGHT_MARGIN = 2, TIME_GAP = 4;
    // The line the player count and its avatar sit on (the Z line), and the gap between them
    private static final int COUNT_LINE = 2, AVATAR_GAP = 3;
    // The widest the coordinates usually get, so without the FPS / TPS line the panel doesn't twitch wider and narrower as they change
    private static final String COORDINATE_TEMPLATE = TextFormatting.GOLD + "X: " + TextFormatting.RESET + "-0000.00 ";
    private static final String TIME_TEMPLATE = "00:00";

    @Inject private static HeadUpDisplay hudData;

    private final UIImage clockImage, playerCountImage;
    private final UILabel coordsLabel, playerCountLabel, timeLabel;
    private final DecimalFormat df = new DecimalFormat("0.##");
    private final DecimalFormat tpsFormat = new DecimalFormat("0.0");

    public UIDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Clock
        this.clockImage = new UIImage(new ItemStack(Items.CLOCK));
        // In the top right corner, so only the first lines need room beside it and the ones below (Z, FPS / TPS) can run underneath
        this.clockImage.setPosition(-RIGHT_MARGIN, ICON_Y, Anchor.TOP | Anchor.RIGHT);

        // The world's time, to the left of the clock
        this.timeLabel = new UILabel("");
        this.timeLabel.setFontOptions(FontColors.WHITE_FO);

        // Player count, on the Z line, with the avatar to its right
        this.playerCountLabel = new UILabel("");
        this.playerCountLabel.setFontOptions(FontColors.WHITE_FO);
        this.playerCountImage = new UIImage(new GuiIcon(new GuiTexture(GuiConfig.Location.GENERIC_AVATAR, 16, 16), 0F, 0F, 1F, 1F));

        // Coordinates
        this.coordsLabel = new UIExpandingLabel("");
        this.coordsLabel.setPosition(TEXT_X, TEXT_Y);
        this.coordsLabel.setFontOptions(FontColors.WHITE_FO);

        this.add(this.clockImage, this.timeLabel, this.playerCountLabel, this.playerCountImage, this.coordsLabel);
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
     * level with the clock and time (top right) need room beside them, the Z line beside the player count and its avatar, the rest only
     * their own width; the widest of them sets the width. It's anchored top right, so it grows down and to the left.
     */
    private void autoSize(final List<String> lines, final boolean stats, final boolean ping) {
        final int lineHeight = this.coordsLabel.getLineHeight();
        final boolean online = this.playerCountImage.isVisible();
        int height = Math.max(HEIGHT, lines.size() * lineHeight + HEIGHT - COORDINATE_LINES * lineHeight);
        // The coordinates, FPS and TPS have no letters below the line, but "Ping" does: room for its "g"
        if (ping) {
            height += DESCENDER;
        }

        // The clock, its margin and the time beside it
        final int clockRoom = RIGHT_MARGIN + ICON_SIZE + TIME_GAP + this.textWidth(TIME_TEMPLATE);
        // Lines that are level with the clock, the rest are below it
        final int besideClock = (int) Math.ceil((ICON_Y + ICON_SIZE - TEXT_Y) / (double) lineHeight);
        final int countRoom = online ? RIGHT_MARGIN + this.avatarSize() + AVATAR_GAP + this.playerCountLabel.getContentWidth() : 0;

        // Without the FPS / TPS line, the coordinates' usual widest keeps the panel from twitching as they change; with it, the panel
        // is exactly as wide as its widest line (normally that bottom line)
        int width = stats ? 0 : TEXT_X + this.textWidth(COORDINATE_TEMPLATE) + GAP + clockRoom;
        for (int i = 0; i < lines.size(); i++) {
            final int room;
            if (i < besideClock) {
                room = GAP + clockRoom;
            } else if (online && i == COUNT_LINE) {
                room = GAP + countRoom;
            } else {
                room = TEXT_X;
            }
            width = Math.max(width, TEXT_X + this.textWidth(lines.get(i)) + room);
        }
        // The panel's padding: its contents (drawn and clipped inside it) need the width above
        final Padding padding = Padding.of(this);
        width += padding.left() + padding.right();

        if (width != this.getWidth() || height != this.getHeight()) {
            this.setSize(width, height);
        }
    }

    private int textWidth(final String text) {
        return (int) Math.ceil(this.coordsLabel.getFont().getStringWidth(text, this.coordsLabel.getFontOptions()));
    }

    /**
     * The font's height: 9 at a scale of 1, of which the letters and digits are the top 7 (the rest is room for descenders).
     */
    private float fontHeight() {
        return this.coordsLabel.getFont().getStringHeight(this.coordsLabel.getFontOptions());
    }

    /**
     * The avatar: 8 at a scale of 1, half of its 16 x 16 texture so it scales cleanly, a pixel taller than the digits beside it.
     */
    private int avatarSize() {
        return Math.round(this.fontHeight() * 8 / 9);
    }

    private void updateCoordinates() {
        final Entity view = MoreObjects.firstNonNull(this.client.getRenderViewEntity(), this.client.player);
        final List<String> lines = new ArrayList<>();
        lines.add(TextFormatting.GOLD + "X: " + TextFormatting.RESET + df.format(view.posX) + " ");
        lines.add(TextFormatting.GOLD + "Y: " + TextFormatting.RESET + df.format(view.posY) + " ");
        lines.add(TextFormatting.GOLD + "Z: " + TextFormatting.RESET + df.format(view.posZ));
        final boolean stats = ClientStaticAccess.configAdapter.get().general.displayPerformanceStats;
        // Ping only means something on a server
        final boolean ping = stats && !this.client.isSingleplayer();
        if (stats) {
            String line = TextFormatting.GOLD + "FPS: " + TextFormatting.RESET + Minecraft.getDebugFPS()
                    + " " + TextFormatting.GOLD + "TPS: " + formatTps(hudData.serverTps);
            if (ping) {
                line += " " + TextFormatting.GOLD + "Ping: " + this.formatPing();
            }
            lines.add(line);
        }
        this.coordsLabel.setText(String.join("\n", lines));
        this.updateTime();
        this.updatePlayerCount();
        this.autoSize(lines, stats, ping);
    }

    /**
     * The world's time of day as hours and minutes on a 24 hour clock (tick 0 is 06:00), level with the middle of the clock.
     */
    private void updateTime() {
        final long ticks = Math.floorMod(this.client.player.world.getWorldTime(), 24000L);
        final int hours = (int) ((ticks / 1000 + 6) % 24);
        final int minutes = (int) (ticks % 1000 * 60 / 1000);
        this.timeLabel.setText(TextFormatting.WHITE + String.format("%02d:%02d", hours, minutes));
        final int labelY = ICON_Y + (ICON_SIZE - this.timeLabel.getLineHeight()) / 2 + 1;
        this.timeLabel.setPosition(BasicScreen.getPaddedX(this.clockImage, TIME_GAP, Anchor.RIGHT), labelY, Anchor.TOP | Anchor.RIGHT);
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

    /**
     * The round trip of Almura's once-a-second ping, green up to 50ms, yellow up to 150ms, red above ("--" until the first answer).
     */
    private String formatPing() {
        final int ping = hudData.ping;
        if (ping < 0) {
            return TextFormatting.GRAY + "--";
        }
        final TextFormatting color = ping <= 50 ? TextFormatting.GREEN : ping <= 150 ? TextFormatting.YELLOW : TextFormatting.RED;
        return color + Integer.toString(ping) + "ms";
    }

    /**
     * The player count at the right of the Z line, with the avatar, as tall as the font, to its right.
     */
    private void updatePlayerCount() {
        final boolean isOnline = !this.client.isSingleplayer() || (this.client.isSingleplayer() && this.client.getIntegratedServer().getPublic());
        if (isOnline) {
            final int lineHeight = this.coordsLabel.getLineHeight();
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + hudData.onlinePlayerCount + "/" + hudData.maxPlayerCount);
            final int y = TEXT_Y + COUNT_LINE * lineHeight;
            final int size = this.avatarSize();
            this.playerCountImage.setSize(size, size);
            // Its bottom on the digits' baseline (the top 7 of the font's 9), so it sits on the line with them
            final int baseline = Math.round(this.fontHeight() * 7 / 9);
            this.playerCountImage.setPosition(-RIGHT_MARGIN, y + baseline - size, Anchor.TOP | Anchor.RIGHT);
            this.playerCountLabel.setPosition(-RIGHT_MARGIN - size - AVATAR_GAP, y, Anchor.TOP | Anchor.RIGHT);
        }
        this.playerCountImage.setVisible(isOnline);
        this.playerCountLabel.setVisible(isOnline);
    }
}
