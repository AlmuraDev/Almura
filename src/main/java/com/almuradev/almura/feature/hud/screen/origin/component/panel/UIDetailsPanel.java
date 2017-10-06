/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.shared.client.GuiConfig;
import com.almuradev.shared.client.ui.FontColors;
import com.almuradev.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.shared.client.ui.screen.SimpleScreen;
import com.google.common.base.MoreObjects;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIDetailsPanel extends UIHUDPanel {

    private final UIImage clockImage, playerCountImage;
    private final UILabel coordsLabel, playerCountLabel;

    public UIDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Clock
        this.clockImage = new UIImage(gui, new ItemStack(Items.CLOCK));
        this.clockImage.setPosition(-2, 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Player Count
        this.playerCountLabel = new UILabel(gui, "");
        this.playerCountLabel.setFontOptions(FontColors.FRO_WHITE);
        this.playerCountImage = new UIImage(gui, new GuiTexture(GuiConfig.Location.GENERIC_AVATAR), null);
        this.playerCountImage.setSize(16, 16);
        this.playerCountImage.setPosition(SimpleScreen.getPaddedX(this.clockImage, 4, Anchor.RIGHT), 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Coordinates
        this.coordsLabel = new UIExpandingLabel(gui, "", true);
        this.coordsLabel.setPosition(2, 2);
        this.coordsLabel.setFontOptions(FontColors.FRO_WHITE);

        this.add(this.clockImage, this.playerCountLabel, this.playerCountImage, this.coordsLabel);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateCoordinates();
        this.updatePlayerCount();
    }

    private void updateCoordinates() {
        final Minecraft client = Minecraft.getMinecraft();
        final Entity view = MoreObjects.firstNonNull(client.getRenderViewEntity(), client.player);
        this.coordsLabel.setText(
                         TextFormatting.GOLD + "X: " + TextFormatting.RESET + String.format("%.3f", view.posX)
                + "\n" + TextFormatting.GOLD + "Y: " + TextFormatting.RESET + String.format("%.3f", view.posY)
                + "\n" + TextFormatting.GOLD + "Z: " + TextFormatting.RESET + String.format("%.3f", view.posZ)
        );
    }

    private void updatePlayerCount() {
        final boolean isOnline = !Minecraft.getMinecraft().isSingleplayer() || (Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft()
                .getIntegratedServer().getPublic());
        if (isOnline) {
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + 1 + "/" + 50);
            this.playerCountLabel.setPosition(SimpleScreen.getPaddedX(this.playerCountImage, 2, Anchor.RIGHT), 3, Anchor.MIDDLE | Anchor.RIGHT);
        }
        this.playerCountImage.setVisible(isOnline);
        this.playerCountLabel.setVisible(isOnline);
    }
}
