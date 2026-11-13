/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.Almura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Optionull;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The player list (held Tab), as on 1.12: a header (Player, Nickname, World), then a row for each player with their face, their name,
 * their nickname when they have one and the world they're in. Scrolls (with the mouse wheel, see {@link Huds}) once it's taller than 175.
 */
final class PlayerListPanel {

    private static final SpriteId ROW_BORDER = SpriteId.of(Almura.ID, "hud/row_border");
    // Sorted as vanilla's list is
    private static final Comparator<PlayerInfo> ORDER = Comparator.<PlayerInfo>comparingInt(p -> -p.getTabListOrder())
        .thenComparingInt(p -> p.getGameMode() == GameType.SPECTATOR ? 1 : 0)
        .thenComparing(p -> Optionull.mapOrDefault(p.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(p -> p.getProfile().name(), String::compareToIgnoreCase);
    private static final float MAX_HEIGHT = 175f, PADDING = 3f, ICON = 12f, ICON_GAP = 2f, COLUMN_GAP = 8f, ROW_GAP = 2f;
    private static final Component HEADER_PLAYER = Component.literal("Player").withStyle(ChatFormatting.GRAY);
    private static final Component HEADER_NICKNAME = Component.literal("Nickname").withStyle(ChatFormatting.GRAY);
    private static final Component HEADER_WORLD = Component.literal("World").withStyle(ChatFormatting.GRAY);
    private static final Component NO_VALUE = Component.literal("-").withStyle(ChatFormatting.DARK_GRAY);
    private static final float SCROLL_STEP = 14f;

    private final ComponentFactory factory;
    private final AnchorPanel panel;
    private final ScrollPanel scroll;
    private final StackPanel rows;
    // What the rows were made from, to make them again only when that changes
    private String signature = "";
    private float contentHeight;

    PlayerListPanel(final ComponentFactory factory, final AnchorPanel panel, final ScrollPanel scroll, final StackPanel rows) {
        this.factory = factory;
        this.panel = panel;
        this.scroll = scroll;
        this.rows = rows;
    }

    static PlayerListPanel create(final ComponentFactory factory, final java.util.function.Function<org.inspirenxe.strata.api.component.Component<?>,
        AnchorPanel> panelOf) {
        final StackPanel rows = factory.stackPanel().setSpacing(ROW_GAP);
        final ScrollPanel scroll = factory.scrollPanel();
        scroll.add(rows);
        final AnchorPanel panel = panelOf.apply(scroll);
        panel.setAnchor(Anchor.TOP_CENTER);
        panel.setVisible(false);
        return new PlayerListPanel(factory, panel, scroll, rows);
    }

    AnchorPanel component() {
        return this.panel;
    }

    /**
     * Shows the list while Tab is held, below the panel at the top middle.
     */
    void refresh(final boolean shown, final float top) {
        this.panel.setVisible(shown);
        if (!shown) {
            return;
        }
        this.panel.setMargin(0f, top, 0f, 0f);
        final Minecraft minecraft = Minecraft.getInstance();
        final List<PlayerInfo> players = minecraft.player.connection.getListedOnlinePlayers().stream().sorted(ORDER).toList();
        final Map<UUID, String> worlds = worlds();

        final List<Component[]> columns = new ArrayList<>();
        final StringBuilder signature = new StringBuilder();
        for (final PlayerInfo player : players) {
            final Component[] row = {name(player), nickname(player), world(worlds.get(player.getProfile().id()))};
            columns.add(row);
            // Its identity too: a player who rejoins gets a new PlayerInfo (and skin) with the same text
            signature.append(player.getProfile().id()).append(System.identityHashCode(player))
                .append(row[0].getString()).append(row[1].getString()).append(row[2].getString()).append('\n');
        }
        if (!signature.toString().equals(this.signature)) {
            this.signature = signature.toString();
            this.rebuild(minecraft.font, players, columns);
        }
        // As tall as the rows, up to 175, then scrolling
        this.scroll.setHeight(Math.min(this.contentHeight, MAX_HEIGHT - PADDING * 2f));
    }

    /**
     * Scrolls the list (the mouse wheel while Tab is held).
     *
     * @return Whether it took the scroll: only while it shows and has more than fits
     */
    boolean scroll(final double notches) {
        if (!this.panel.isVisible() || this.scroll.maxScrollY() <= 0f) {
            return false;
        }
        this.scroll.scrollBy(0f, (float) (-notches * SCROLL_STEP));
        return true;
    }

    private void rebuild(final Font font, final List<PlayerInfo> players, final List<Component[]> columns) {
        // Each column as wide as its widest value, header included
        final float[] widths = {font.width(HEADER_PLAYER), font.width(HEADER_NICKNAME), font.width(HEADER_WORLD)};
        for (final Component[] row : columns) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = Math.max(widths[i], font.width(row[i]));
            }
        }
        this.rows.clear();
        this.rows.add(this.row(null, new Component[] {HEADER_PLAYER, HEADER_NICKNAME, HEADER_WORLD}, widths));
        for (int i = 0; i < players.size(); i++) {
            this.rows.add(this.row(players.get(i), columns.get(i), widths));
        }
        final float rowHeight = font.lineHeight + 5f;
        this.contentHeight = (players.size() + 1) * rowHeight + players.size() * ROW_GAP;
    }

    /**
     * A row: the face, then the columns at their widths. The header has no face and no border.
     */
    private AnchorPanel row(final PlayerInfo player, final Component[] columns, final float[] widths) {
        final ComponentFactory factory = this.factory;
        final StackPanel line = factory.stackPanel(Orientation.HORIZONTAL);
        final Image face = factory.image();
        face.setImageSize(ICON, ICON).setSize(ICON, ICON);
        if (player != null) {
            face.setVisual(MinecraftVisuals.playerFace(player.getSkin()));
        }
        line.add(face.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE).setMargin(0f, 0f, ICON_GAP, 0f));
        for (int i = 0; i < columns.length; i++) {
            final Label label = factory.label(MinecraftText.of(columns[i]));
            label.setWidth(widths[i]);
            line.add(label.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
                .setMargin(0f, 0f, i < columns.length - 1 ? COLUMN_GAP : 0f, 0f));
        }
        final AnchorPanel row = factory.anchorPanel();
        if (player != null) {
            final Image border = factory.image(ROW_BORDER).setImageSize(1f, 1f).setScaling(Image.Scaling.STRETCH);
            row.add(border.setAnchor(Anchor.FILL));
        }
        row.add(line.setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 2f, 0f));
        row.setHeight(Minecraft.getInstance().font.lineHeight + 5f);
        return row;
    }

    private static Component name(final PlayerInfo player) {
        final MutableComponent name = Component.literal(player.getProfile().name()).withStyle(ChatFormatting.WHITE);
        return player.getGameMode() == GameType.SPECTATOR ? name.withStyle(ChatFormatting.ITALIC) : name;
    }

    /**
     * The name the server shows the player by (a nickname) when it isn't just their username.
     */
    private static Component nickname(final PlayerInfo player) {
        final Component shown = player.getTabListDisplayName();
        if (shown == null || shown.getString().equals(player.getProfile().name())) {
            return NO_VALUE;
        }
        return shown;
    }

    private static Component world(final String world) {
        return world == null || world.isEmpty() ? NO_VALUE : Component.literal(world).withStyle(ChatFormatting.AQUA);
    }

    /**
     * Each player's world, as Almura's server tells them; until it does, playing on this computer, read from the game's own server.
     */
    private static Map<UUID, String> worlds() {
        final Map<UUID, String> sent = ServerHudData.playerWorlds();
        if (sent != null) {
            return sent;
        }
        final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        final Map<UUID, String> worlds = new HashMap<>();
        if (server != null) {
            for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
                worlds.put(player.getUUID(), OriginHud.worldName(server, player.level().dimension()));
            }
        }
        return worlds;
    }
}
