/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.client.gui.UIAvatarImage;
import com.almuradev.almura.client.gui.component.UISimpleList;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UIPlayerListPanel extends UIHUDPanel {

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
    private final Minecraft client = Minecraft.getMinecraft();
    private final UISimpleList playerList;

    @SuppressWarnings("unchecked")
    public UIPlayerListPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.playerList = new UISimpleList(gui, 0, 0);
        this.playerList.getScrollBar().setAutoHide(false);
        this.playerList.setElementSpacing(2);
        this.playerList.register(this);
        this.add(this.playerList);
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
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

        // Get maximum column width
        final int maxColumnWidth = this.getMaxColumnWidth(entries);

        final List<PlayerListElement> elementList = new ArrayList<>();
        for (int i = 0; i < entries.size(); i += 2) {
            final NetworkPlayerInfo player1 = entries.get(i);
            final NetworkPlayerInfo player2 = i + 1 < entries.size() ? entries.get(i + 1) : null;

            // Add a new element tot he list
            elementList.add(new PlayerListElement(this.getGui(), this.playerList, player1, player2,
                    maxColumnWidth));
        }

        // Set elements
        this.playerList.setElements(elementList);

        // Auto size our height
        this.height = Math.min(this.playerList.getContentHeight() + 5, MAX_HEIGHT);
        this.playerList.getScrollBar().setVisible(this.height >= MAX_HEIGHT);

        // Auto size our width
        this.width = entries.size() == 1 ? maxColumnWidth + 12 : (maxColumnWidth + (!this.playerList.getScrollBar().isVisible() ? 9 : 11)) * 2 + 1;

        // Resize list
        this.playerList.setSize(this.width - (this.playerList.getScrollBar().isVisible() ? 7 : 5), this.height - 6);
    }

    private int getMaxColumnWidth(final List<NetworkPlayerInfo> players) {
        int maxWidth = 0;
        for (final NetworkPlayerInfo player : players) {
            maxWidth = Math.max(maxWidth, this.client.fontRenderer.getStringWidth(getTrimmedDisplayName(player)));
        }

        return maxWidth + 11;
    }

    // TODO: this does not properly take colours into account
    private static String getTrimmedDisplayName(final NetworkPlayerInfo player) {
        String name = DEFAULT_COLOR + getDisplayName(player);
        if (player.getGameType() == GameType.SPECTATOR) {
            name = TextFormatting.ITALIC + name;
        }
        return name.length() >= MAX_DISPLAY_NAME_LENGTH ? name.substring(0, MAX_DISPLAY_NAME_LENGTH_SUBSTRING) + DISPLAY_NAME_TRAILING_DOTS : name;
    }

    private static String getDisplayName(final NetworkPlayerInfo player) {
        if (player.getDisplayName() != null) {
            return player.getDisplayName().getFormattedText();
        }
        return ScorePlayerTeam.formatPlayerName(player.getPlayerTeam(), player.getGameProfile().getName());
    }

    protected static final class PlayerListElement extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int ICON_SIZE = 12;
        private final UIAvatarImage avatarImage1;
        private final int maxColumnWidth;
        private final NetworkPlayerInfo player1;
        @Nullable private final NetworkPlayerInfo player2;
        @Nullable private UIAvatarImage avatarImage2;

        @SuppressWarnings("deprecation")
        private PlayerListElement(MalisisGui gui, UISimpleList parent, NetworkPlayerInfo player1, @Nullable NetworkPlayerInfo player2, int maxColumnWidth) {
            super(gui);
            this.parent = parent;
            this.maxColumnWidth = maxColumnWidth;

            this.player1 = player1;
            this.player2 = player2;

            this.avatarImage1 = new UIAvatarImage(gui, this.player1);
            this.avatarImage1.setSize(12, 12);
            this.add(this.avatarImage1);

            if (this.player2 != null) {
                this.avatarImage2 = new UIAvatarImage(gui, this.player2);
                this.avatarImage2.setSize(12, 12);
                this.avatarImage2.setPosition(this.x + this.maxColumnWidth + 6, 0);
                this.add(this.avatarImage2);
            }

            // Auto size
            final int width = player2 == null ? this.maxColumnWidth + 6 : this.maxColumnWidth * 2 + 13;
            this.setSize(width, Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 5);

            // Set padding
            this.setPadding(1, 1);

            // Set alphas and colors
            this.setBackgroundAlpha(0);
            this.setBorder(BORDER_COLOR, 1, 75);
        }

        @Override
        public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            if (!(this.parent instanceof UISimpleList)) {
                return;
            }

            final UISimpleList parent = (UISimpleList) this.parent;

            // Adjust width for scrollbar
            final int width = parent.getContentWidth() - (parent.getScrollBar().isDisabled() ? 0 : parent.getScrollBar().getRawWidth() + 1);
            this.setSize(width, this.getHeight());

            // Call to the super and draw
            super.drawBackground(renderer, mouseX, mouseY, partialTick);

            int x = this.horizontalPadding + 2;
            final int y = this.verticalPadding + 2;

            // Text
            renderer.drawText(UIPlayerListPanel.getTrimmedDisplayName(this.player1), x + ICON_SIZE, y, this.zIndex);

            // Draw player 2 if needed
            if (this.player2 != null) {
                x += this.maxColumnWidth + 4;

                // Text
                renderer.drawText(UIPlayerListPanel.getTrimmedDisplayName(this.player2), x + ICON_SIZE + 2, y, this.zIndex);

                // Separator
                renderer.drawRectangle(this.maxColumnWidth + 5 + this.horizontalPadding, 1, this.zIndex, 1,
                        this.height - 2, BORDER_COLOR, 75);
            }
        }
    }
}
