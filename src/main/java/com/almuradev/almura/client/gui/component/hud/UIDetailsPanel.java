/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.UIExpandingLabel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.text.NumberFormat;
import java.util.Locale;

public class UIDetailsPanel extends UIHUDPanel {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    private final UIImage clockImage, playerCountImage;
    private final UILabel coordsLabel, playerCountLabel;

    public UIDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Clock
        this.clockImage = new UIImage(gui, new ItemStack(Items.CLOCK));
        this.clockImage.setPosition(-2, 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Player Count
        this.playerCountLabel = new UILabel(gui, "");
        this.playerCountLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);
        this.playerCountImage = new UIImage(gui, new GuiTexture(Constants.Gui.AVATAR_GENERIC_LOCATION), null);
        this.playerCountImage.setSize(16, 16);
        this.playerCountImage.setPosition(SimpleScreen.getPaddedX(this.clockImage, 4, Anchor.RIGHT), 0, Anchor.MIDDLE | Anchor.RIGHT);

        // Coordinates
        this.coordsLabel = new UIExpandingLabel(gui, "", true);
        this.coordsLabel.setPosition(2, 2);
        this.coordsLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        this.add(this.clockImage, this.playerCountLabel, this.playerCountImage, this.coordsLabel);
    }

    public void updateCoordinates() {
        this.coordsLabel.setText(
                TextFormatting.GOLD + "X: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getX()) +
                        "\n" +
                        TextFormatting.GOLD + "Y: " + TextFormatting.RESET + NUMBER_FORMAT
                        .format(Minecraft.getMinecraft().player.getPosition().getY()) +
                        "\n" +
                        TextFormatting.GOLD + "Z: " + TextFormatting.RESET + NUMBER_FORMAT
                        .format(Minecraft.getMinecraft().player.getPosition().getZ()));
    }

    public void updatePlayerCount() {
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
