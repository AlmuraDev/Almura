/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.hud.screen.origin.UIAvatarImage;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class UIPlayerListPanel extends AbstractPanel {

    @Inject private static ClientNickManager manager;

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
    private final UISimpleList<PlayerListElementData> playerList;

    @SuppressWarnings("unchecked")
    public UIPlayerListPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.playerList = new UISimpleList<>(gui, 0, 0);
        this.playerList.setComponentFactory(PlayerListElement::new);
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

        final List<PlayerListElementData> elementList = new ArrayList<>();
        for (int i = 0; i < entries.size(); i += 2) {
            final NetworkPlayerInfo player1 = entries.get(i);
            final NetworkPlayerInfo player2 = i + 1 < entries.size() ? entries.get(i + 1) : null;

            // Add a new element to the list
            elementList.add(new PlayerListElementData(this.getGui(), this.playerList, player1, player2, maxColumnWidth));
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

    private static String getTrimmedDisplayName(final NetworkPlayerInfo player) {
        String name = DEFAULT_COLOR + getDisplayName(player);
        if (player.getGameType() == GameType.SPECTATOR) {
            name = TextFormatting.ITALIC + name;
        }
        return name.length() >= MAX_DISPLAY_NAME_LENGTH ? name.substring(0, MAX_DISPLAY_NAME_LENGTH_SUBSTRING) + DISPLAY_NAME_TRAILING_DOTS : name;
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
            return player.getDisplayName().getFormattedText();
        }

        return ScorePlayerTeam.formatPlayerName(player.getPlayerTeam(), player.getGameProfile().getName());
    }

    protected static final class PlayerListElement extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int ICON_SIZE = 12;
        private final PlayerListElementData elementData;

        @SuppressWarnings("deprecation")
        private PlayerListElement(MalisisGui gui, PlayerListElementData elementData) {
            super(gui);
            this.parent = elementData.getParent();
            this.elementData = elementData;

            this.add(this.elementData.getPlayer1Image());

            if (this.elementData.getPlayer2Info() != null && this.elementData.getPlayer2Image() != null) {
                this.elementData.getPlayer2Image().setPosition(this.x + this.elementData.getMaxColumnWidth() + 6, 0);
                this.add(this.elementData.getPlayer2Image());
            }

            // Auto size
            final int width = this.elementData.getPlayer2Info() == null
                    ? this.elementData.getMaxColumnWidth() + 6
                    : this.elementData.getMaxColumnWidth() * 2 + 13;
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
            final int width = parent.getContentWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 1 : 0);
            this.setSize(width, this.getHeight());

            // Call to the super and draw
            super.drawBackground(renderer, mouseX, mouseY, partialTick);

            int x = getLeftPadding() + 2;
            final int y = getTopPadding() + 2;

            // Text
            renderer.drawText(UIPlayerListPanel.getTrimmedDisplayName(this.elementData.getPlayer1Info()), x + ICON_SIZE, y, this.zIndex);

            // Draw player 2 if needed
            if (this.elementData.getPlayer2Info() != null) {
                x += this.elementData.getMaxColumnWidth() + 4;

                // Text
                renderer.drawText(UIPlayerListPanel.getTrimmedDisplayName(this.elementData.getPlayer2Info()), x + ICON_SIZE + 2, y, this.zIndex);

                // Separator
                renderer.drawRectangle(this.elementData.getMaxColumnWidth() + 5 + this.getLeftPadding(), 1, this.zIndex, 1, this.height - 2,
                        BORDER_COLOR, 75);
            }
        }
    }

    protected static class PlayerListElementData {

        private final UISimpleList parent;
        private final int maxColumnWidth;
        private final UIAvatarImage player1Image;
        private final NetworkPlayerInfo player1Info;
        @Nullable private NetworkPlayerInfo player2Info;
        @Nullable private UIAvatarImage player2Image;

        protected PlayerListElementData(MalisisGui gui, UISimpleList parent, NetworkPlayerInfo player1Info, @Nullable NetworkPlayerInfo player2Info,
                int maxColumnWidth) {
            this.parent = parent;
            this.maxColumnWidth = maxColumnWidth;
            this.player1Info = player1Info;
            this.player2Info = player2Info;
            this.player1Image = new UIAvatarImage(gui, this.player1Info);
            this.player1Image.setSize(12, 12);
            if (this.player2Info != null) {
                this.player2Image = new UIAvatarImage(gui, this.player2Info);
                this.player2Image.setSize(12, 12);
            }
        }

        public UISimpleList getParent() {
            return this.parent;
        }

        public UIAvatarImage getPlayer1Image() {
            return this.player1Image;
        }

        @Nullable
        public UIAvatarImage getPlayer2Image() {
            return this.player2Image;
        }

        public NetworkPlayerInfo getPlayer1Info() {
            return this.player1Info;
        }

        @Nullable
        public NetworkPlayerInfo getPlayer2Info() {
            return this.player2Info;
        }

        public int getMaxColumnWidth() {
            return this.maxColumnWidth;
        }
    }
}
