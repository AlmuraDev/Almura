/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.asm.mixin.interfaces.IMixinEntityPlayer;
import com.almuradev.almura.client.gui.UIAvatarImage;
import com.almuradev.almura.client.gui.component.UISimpleList;
import com.google.common.collect.Lists;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UIPlayerListPanel extends UIHUDPanel {

    private final Minecraft client = Minecraft.getMinecraft();
    private UISimpleList playerList;

    @SuppressWarnings("unchecked")
    public UIPlayerListPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.iconProvider = new GuiIconProvider(Constants.Gui.ICON_VANILLA_CONTAINER_INVENTORY_BLUE);

        this.playerList = new UISimpleList(gui, 0, 0);
        this.playerList.getScrollBar().setAutoHide(false);
        this.playerList.setElementSpacing(1);
        this.playerList.register(this);
        this.add(this.playerList);
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
        this.playerList.onScrollWheel(x, y, delta);
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final List<EntityPlayer> list = new ArrayList<>(this.client.world.playerEntities);
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));

        final List<PlayerListElement> elementList = Lists.newArrayList();

        // Get maximum column width
        final int maxColumnWidth = getMaxColumnWidth(list);

        for (int i = 0; i < list.size(); i += 2) {
            final EntityPlayer player1 = list.get(i);
            final EntityPlayer player2 = i + 1 < list.size() ? list.get(i + 1) : null;

            // Add a new element tot he list
            elementList.add(new PlayerListElement(this.getGui(), this.playerList, (AbstractClientPlayer) player1, (AbstractClientPlayer) player2,
                    maxColumnWidth));
        }

        // Set elements
        this.playerList.setElements(elementList);

        // Auto size our height
        this.height = Math.min(this.playerList.getContentHeight() + 5, 215);
        this.playerList.getScrollBar().setVisible(this.height >= 215);

        // Auto size our width
        this.width = list.size() == 1 ? maxColumnWidth + 12 : (maxColumnWidth + (!this.playerList.getScrollBar().isVisible() ? 9 : 11)) * 2 + 1;

        // Resize list
        this.playerList.setSize(this.width - (this.playerList.getScrollBar().isVisible() ? 7 : 5), this.height - 6);
    }

    private int getMaxColumnWidth(List<EntityPlayer> players) {
        int maxWidth = 0;
        for (EntityPlayer player : players) {
            maxWidth = Math.max(maxWidth, this.client.fontRenderer.getStringWidth(getFormattedDisplayName(player)));
        }

        return maxWidth + 11;
    }

    @SuppressWarnings("deprecation")
    private static String getFormattedDisplayName(EntityPlayer player) {
        final IMixinEntityPlayer mixPlayer = (IMixinEntityPlayer) player;
        final String serializedText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, mixPlayer.getPrefix(), player
                .getName(), mixPlayer.getSuffix()));
        return serializedText.length() >= 26 ? serializedText.substring(0, 23) + "..." : serializedText;
    }

    @Override
    public float getScrollStep() {
        return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.075F);
    }

    protected static final class PlayerListElement extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int ICON_SIZE = 12;
        private final UIAvatarImage avatarImage1;
        private final int maxColumnWidth;
        private final AbstractClientPlayer player1;
        @Nullable private final AbstractClientPlayer player2;
        @Nullable private UIAvatarImage avatarImage2;

        @SuppressWarnings("deprecation")
        private PlayerListElement(MalisisGui gui, UISimpleList parent, AbstractClientPlayer player1, @Nullable AbstractClientPlayer player2, int maxColumnWidth) {
            super(gui);

            // Set properties
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
                this.avatarImage2.setPosition(x + this.maxColumnWidth + 6, 0);
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
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UISimpleList) {
                final UISimpleList parent = (UISimpleList) this.parent;

                // Adjust width for scrollbar
                final int width = parent.getContentWidth() - (parent.getScrollBar().isDisabled() ? 0 : parent.getScrollBar().getRawWidth() + 1);
                setSize(width, getHeight());

                // Call to the super and draw
                super.drawBackground(renderer, mouseX, mouseY, partialTick);

                int x = this.horizontalPadding + 2;
                int y = this.verticalPadding + 2;

                // Text
                renderer.drawText(UIPlayerListPanel.getFormattedDisplayName(this.player1), x + ICON_SIZE, y, this.zIndex);

                // Draw player 2 if needed
                if (this.player2 != null) {
                    x += this.maxColumnWidth + 4;

                    // Text
                    renderer.drawText(UIPlayerListPanel.getFormattedDisplayName(this.player2), x + ICON_SIZE + 2, y, this.zIndex);

                    // Separator
                    renderer.drawRectangle(maxColumnWidth + 5 + this.horizontalPadding, 1, this.zIndex, 1,
                            this.height - 2, BORDER_COLOR, 75);
                }
            }
        }
    }
}
