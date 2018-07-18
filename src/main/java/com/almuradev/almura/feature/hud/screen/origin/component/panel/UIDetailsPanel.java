/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.base.MoreObjects;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIDetailsPanel extends AbstractPanel {

    @Inject private static HeadUpDisplay hudData;

    private final UIImage clockImage, playerCountImage;
    private final UILabel coordsLabel, playerCountLabel;
    private final DecimalFormat df = new DecimalFormat("0.##");

    public UIDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Clock
        this.clockImage = new UIImage(gui, new ItemStack(Items.CLOCK));
        this.clockImage.setPosition(-2, 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Player Count
        this.playerCountLabel = new UILabel(gui, "");
        this.playerCountLabel.setFontOptions(FontColors.WHITE_FO);
        this.playerCountImage = new UIImage(gui, new GuiTexture(GuiConfig.Location.GENERIC_AVATAR), null);
        this.playerCountImage.setSize(16, 16);
        this.playerCountImage.setPosition(SimpleScreen.getPaddedX(this.clockImage, 4, Anchor.RIGHT), 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Coordinates
        this.coordsLabel = new UIExpandingLabel(gui, "", true);
        this.coordsLabel.setPosition(2, 2);
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
        this.updatePlayerCount();
    }

    private void updateCoordinates() {
        final Entity view = MoreObjects.firstNonNull(this.client.getRenderViewEntity(), this.client.player);
        this.coordsLabel.setText(
                         TextFormatting.GOLD + "X: " + TextFormatting.RESET + df.format(view.posX) + " "
                + "\n" + TextFormatting.GOLD + "Y: " + TextFormatting.RESET + df.format(view.posY) + " "
                + "\n" + TextFormatting.GOLD + "Z: " + TextFormatting.RESET + df.format(view.posZ)
        );
    }

    private void updatePlayerCount() {
        final boolean isOnline = !this.client.isSingleplayer() || (this.client.isSingleplayer() && this.client.getIntegratedServer().getPublic());
        if (isOnline) {
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + hudData.onlinePlayerCount + "/" + hudData.maxPlayerCount);
            this.playerCountLabel.setPosition(SimpleScreen.getPaddedX(this.playerCountImage, 2, Anchor.RIGHT), 1, Anchor.MIDDLE | Anchor.RIGHT);
        }
        this.playerCountImage.setVisible(isOnline);
        this.playerCountLabel.setVisible(isOnline);
    }
}
