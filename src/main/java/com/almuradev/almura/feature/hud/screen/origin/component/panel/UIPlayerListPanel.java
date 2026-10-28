/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.screen.origin.UIAvatarImage;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * The player list shown while the player list key (tab) is held. One player per row: avatar, username, nickname and current world.
 */
public class UIPlayerListPanel extends AbstractPanel {

    @Inject private static ClientNickManager manager;
    @Inject private static HeadUpDisplay hudData;

    private static final Ordering<NetworkPlayerInfo> ORDERING = Ordering.from((p1, p2) -> {
        final ScorePlayerTeam t1 = p1.getPlayerTeam();
        final ScorePlayerTeam t2 = p2.getPlayerTeam();
        return ComparisonChain.start()
                .compareTrueFirst(p1.getGameType() != GameType.SPECTATOR, p2.getGameType() != GameType.SPECTATOR)
                .compare(t1 != null ? t1.getName() : "", t2 != null ? t2.getName() : "")
                .compare(p1.getGameProfile().getName(), p2.getGameProfile().getName())
                .result();
    });
    private static final int MAX_DISPLAY_NAME_LENGTH = 26;
    private static final int DISPLAY_NAME_TRAILING_DOTS_AMOUNT = 3;
    private static final String DISPLAY_NAME_TRAILING_DOTS = Strings.repeat(".", DISPLAY_NAME_TRAILING_DOTS_AMOUNT);
    private static final int MAX_DISPLAY_NAME_LENGTH_SUBSTRING = MAX_DISPLAY_NAME_LENGTH - DISPLAY_NAME_TRAILING_DOTS_AMOUNT;
    private static final int MAX_HEIGHT = 175;
    private static final TextFormatting DEFAULT_COLOR = TextFormatting.WHITE;
    private static final String HEADER_PLAYER = TextFormatting.GRAY + "Player";
    private static final String HEADER_NICKNAME = TextFormatting.GRAY + "Nickname";
    private static final String HEADER_WORLD = TextFormatting.GRAY + "World";
    private static final String NO_VALUE = TextFormatting.DARK_GRAY + "-";
    private static final int ICON_SIZE = 12;
    private static final int COLUMN_GAP = 8;

    private final BasicList<PlayerRow> playerList;
    // Rows (and their avatar images) are only rebuilt when something shown in the list changes, not every frame
    @Nullable private String rowsSignature;
    private int[] columnWidths = new int[3];

    @SuppressWarnings("unchecked")
    public UIPlayerListPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.playerList = new BasicList<>(0, 0);
        this.playerList.setItemComponentFactory(PlayerListElement::new);
        this.playerList.getScrollBar().autoHide = false;
        this.playerList.setItemComponentSpacing(2);
        this.playerList.register(this);
        this.add(this.playerList);
    }

    @Override
    public boolean onScrollWheel(int delta) {
        this.playerList.getScrollBar().scrollBy(-delta * this.getScrollStep());
        return true;
    }

    @Override
    public float getScrollStep() {
        return GuiScreen.isCtrlKeyDown() ? 0.125f : 0.075f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final List<NetworkPlayerInfo> entries = ORDERING.sortedCopy(this.client.player.connection.getPlayerInfoMap());

        final List<String[]> columns = new ArrayList<>(entries.size());
        final StringBuilder signature = new StringBuilder();
        for (final NetworkPlayerInfo player : entries) {
            final String[] row = {getUsername(player), getNickname(player), getWorld(player)};
            columns.add(row);
            // Identity too: a player who rejoins gets a new NetworkPlayerInfo (skin) with the same text
            signature.append(player.getGameProfile().getId()).append('\0').append(System.identityHashCode(player)).append('\0')
                    .append(row[0]).append('\0').append(row[1]).append('\0').append(row[2]).append('\n');
        }

        if (!signature.toString().equals(this.rowsSignature)) {
            this.rowsSignature = signature.toString();
            this.rebuildRows(entries, columns);
        }

        // Auto size our height
        this.height = Math.min(this.playerList.getContentHeight() + 5, MAX_HEIGHT);
        this.playerList.getScrollBar().setVisible(this.height >= MAX_HEIGHT);

        // Auto size our width
        this.width = rowWidth(this.columnWidths) + (!this.playerList.getScrollBar().isVisible() ? 9 : 11);

        // Resize list
        this.playerList.setSize(this.width - (this.playerList.getScrollBar().isVisible() ? 7 : 5), this.height - 6);
    }

    private void rebuildRows(final List<NetworkPlayerInfo> entries, final List<String[]> columns) {
        final FontRenderer font = this.client.fontRenderer;

        // Column widths fit the widest value in each column, including the header
        final int[] widths = {font.getStringWidth(HEADER_PLAYER), font.getStringWidth(HEADER_NICKNAME), font.getStringWidth(HEADER_WORLD)};
        for (final String[] row : columns) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = Math.max(widths[i], font.getStringWidth(row[i]));
            }
        }
        this.columnWidths = widths;

        final List<PlayerRow> rows = new ArrayList<>(entries.size() + 1);
        rows.add(new PlayerRow(this.getGui(), null, new String[] {HEADER_PLAYER, HEADER_NICKNAME, HEADER_WORLD}, widths));
        for (int i = 0; i < entries.size(); i++) {
            rows.add(new PlayerRow(this.getGui(), entries.get(i), columns.get(i), widths));
        }

        this.playerList.setItems(rows);
    }

    private static int rowWidth(final int[] widths) {
        return ICON_SIZE + 2 + widths[0] + COLUMN_GAP + widths[1] + COLUMN_GAP + widths[2] + 4;
    }

    private static String getUsername(final NetworkPlayerInfo player) {
        final String name = DEFAULT_COLOR + player.getGameProfile().getName();
        return player.getGameType() == GameType.SPECTATOR ? TextFormatting.ITALIC + name : name;
    }

    private static String getNickname(final NetworkPlayerInfo player) {
        final String displayName = getDisplayName(player);
        // No nickname set: the display name is just the (possibly team formatted) username
        if (TextFormatting.getTextWithoutFormattingCodes(displayName).equals(player.getGameProfile().getName())) {
            return NO_VALUE;
        }
        return trim(DEFAULT_COLOR + displayName);
    }

    private static String getWorld(final NetworkPlayerInfo player) {
        final String world = hudData == null ? null : hudData.playerWorlds.get(player.getGameProfile().getId());
        return world == null || world.isEmpty() ? NO_VALUE : trim(TextFormatting.AQUA + world);
    }

    private static String trim(final String name) {
        // TODO Find a way to trim nicknames with formating codes
        return TextFormatting.getTextWithoutFormattingCodes(name).length() >= MAX_DISPLAY_NAME_LENGTH
                ? name.substring(0, Math.min(name.length(), MAX_DISPLAY_NAME_LENGTH_SUBSTRING)) + DISPLAY_NAME_TRAILING_DOTS
                : name;
    }

    private static String getDisplayName(final NetworkPlayerInfo player) {

        if (player.getDisplayName() != null) {
            // TODO This is a bruteforce hack to fix race conditions where our tab list won't have the right info or gets wiped out by Vanilla
            // Basically username = displayname, lookup their nick in the manager
            if (player.getGameProfile().getName().equals(player.getDisplayName().getUnformattedText())) {
                final Text nick = manager.getNicknameFor(player.getGameProfile().getId());
                if (nick != null) {
                    player.setDisplayName(SpongeTexts.toComponent(nick));
                }
            }
        } else {
            final Text nick = manager.getNicknameFor(player.getGameProfile().getId());
            if (nick != null) {
                player.setDisplayName(SpongeTexts.toComponent(nick));
            }
        }

        if (player.getDisplayName() != null) {
            // The below is to fix the odd RESET value stored within the Nickname from Nucleus that causes issues with this module.
            String fixedFormattedDisplayName = player.getDisplayName().getFormattedText();
            if (fixedFormattedDisplayName.startsWith("~") && fixedFormattedDisplayName.length() >= 3) {
                fixedFormattedDisplayName = "~" + fixedFormattedDisplayName.substring(3);
            }

            return fixedFormattedDisplayName;
        }

        return ScorePlayerTeam.formatPlayerName(player.getPlayerTeam(), player.getGameProfile().getName());
    }

    protected static final class PlayerListElement extends BasicList.ItemComponent<PlayerRow> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private final PlayerRow row;

        public PlayerListElement(final MalisisGui gui, final BasicList<PlayerRow> parent, final PlayerRow item) {
            super(gui, parent, item);

            this.parent = parent;
            this.row = item;

            if (this.row.getImage() != null) {
                this.add(this.row.getImage());
            }

            // Auto size
            this.setSize(rowWidth(this.row.getColumnWidths()), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 5);

            // Set padding
            this.setPadding(1, 1);

            // Set alphas and colors
            this.setBackgroundAlpha(0);
            this.setBorder(BORDER_COLOR, 1, this.row.isHeader() ? 0 : 75);

            // The columns, under the avatar (the row's width follows the list, see ItemComponent)
            this.setForeground(renderer -> {
                this.drawColumns(renderer);
                this.content.render(renderer);
            });
        }

        private void drawColumns(final GuiRenderer renderer) {
            final int[] widths = this.row.getColumnWidths();
            final String[] columns = this.row.getColumns();
            int x = getLeftPadding() + 2 + ICON_SIZE;
            final int y = getTopPadding() + 2;

            for (int i = 0; i < columns.length; i++) {
                renderer.drawText(columns[i], x, y, this.zIndex);
                x += widths[i] + COLUMN_GAP;
            }
        }
    }

    protected static final class PlayerRow {

        @Nullable private final NetworkPlayerInfo info;
        @Nullable private final UIAvatarImage image;
        private final String[] columns;
        private final int[] columnWidths;

        /**
         * @param info the player, or null for the header row
         */
        PlayerRow(final MalisisGui gui, @Nullable final NetworkPlayerInfo info, final String[] columns, final int[] columnWidths) {
            this.info = info;
            this.columns = columns;
            this.columnWidths = columnWidths;
            if (info != null) {
                this.image = new UIAvatarImage(gui, info);
                this.image.setSize(ICON_SIZE, ICON_SIZE);
            } else {
                this.image = null;
            }
        }

        boolean isHeader() {
            return this.info == null;
        }

        @Nullable
        UIAvatarImage getImage() {
            return this.image;
        }

        String[] getColumns() {
            return this.columns;
        }

        int[] getColumnWidths() {
            return this.columnWidths;
        }
    }
}
