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
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.NumberFormat;
import java.util.Locale;

@SideOnly(Side.CLIENT)
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
        this.coordsLabel.setText(
                TextFormatting.GOLD + "X: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getX()) +
                        "\n" +
                        TextFormatting.GOLD + "Y: " + TextFormatting.RESET + NUMBER_FORMAT
                        .format(Minecraft.getMinecraft().player.getPosition().getY()) +
                        "\n" +
                        TextFormatting.GOLD + "Z: " + TextFormatting.RESET + NUMBER_FORMAT
                        .format(Minecraft.getMinecraft().player.getPosition().getZ()));
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
