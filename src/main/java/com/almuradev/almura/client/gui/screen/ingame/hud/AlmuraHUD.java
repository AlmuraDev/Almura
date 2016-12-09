/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.component.UIExpandingLabel;
import com.almuradev.almura.client.gui.component.UIPropertyBar;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

import java.text.NumberFormat;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class AlmuraHUD extends AbstractHUD {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    private UILabel compassLabel, coordsLabel, currencyLabel, playerCountLabel, playerLabel, timeLabel, worldLabel;
    private UIImage timeImage, currencyImage, playerCountImage, playerImage;
    private UIBackgroundContainer leftContainer, middleContainer, rightContainer;

    @Override
    public void construct() {
        guiscreenBackground = false;

        this.leftContainer = new UIBackgroundContainer(this, 125, 22);
        this.leftContainer.setPosition(-1, -1, Anchor.TOP | Anchor.LEFT);
        this.leftContainer.setPadding(1, 1);
        this.leftContainer.setBackgroundAlpha(180);
        this.leftContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        this.leftContainer.setBorder(Color.ofRgb(70, 70, 70).getRgb(), 1, 200);

        this.playerImage = new UIImage(this, new GuiRemoteTexture(
                GuiConstants.AVATAR_GENERIC_LOCATION,
                new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + this.mc.player.getUniqueID() + ".png"),
                String.format(GuiConstants.SKIN_URL_BASE, this.mc.player.getUniqueID().toString(), 16),
                16, 16), null);
        this.playerImage.setPosition(2, 2);
        this.playerImage.setSize(16, 16);

        this.playerLabel = new UILabel(this, TextFormatting.WHITE + this.mc.player.getName());
        this.playerLabel.setPosition(SimpleScreen.getPaddedX(this.playerImage, 4), 2);

        this.currencyImage = new UIImage(this, MalisisGui.BLOCK_TEXTURE, Icon.from(Items.EMERALD));
        this.currencyImage.setPosition(SimpleScreen.getPaddedX(this.playerImage, 2), -1, Anchor.BOTTOM | Anchor.LEFT);
        this.currencyImage.setSize(8, 8);

        this.currencyLabel = new UILabel(this, "");
        this.currencyLabel.setPosition(SimpleScreen.getPaddedX(this.currencyImage, 0), -3, Anchor.BOTTOM | Anchor.LEFT);
        this.currencyLabel.setFontOptions(FontOptions.builder()
                .from(FontOptionsConstants.FRO_COLOR_WHITE)
                .scale(0.50f)
                .build());

        this.leftContainer.add(this.playerImage, this.playerLabel, this.currencyImage, this.currencyLabel);

        final UIBackgroundContainer tempContainer = new UIBackgroundContainer(this, 79, 44);
        tempContainer.setPosition(-1, SimpleScreen.getPaddedY(this.leftContainer, -1), Anchor.TOP | Anchor.LEFT);
        tempContainer.setPadding(1, 1);
        tempContainer.setBackgroundAlpha(180);
        tempContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        tempContainer.setBorder(Color.ofRgb(70, 70, 70).getRgb(), 1, 200);

        final UIPropertyBar healthBar = new UIPropertyBar(this, 75, 9);
        healthBar.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        healthBar.setColor(Color.ofRgb(187, 19, 19).getRgb());
        healthBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_HEART_BACKGROUND);
        healthBar.setForegroundIcon(GuiConstants.VANILLA_ICON_HEART_FOREGROUND);
        healthBar.setAmount(1.0f);

        final UIPropertyBar armorBar = new UIPropertyBar(this, 75, 9);
        armorBar.setPosition(0, SimpleScreen.getPaddedY(healthBar, 1), Anchor.CENTER | Anchor.TOP);
        armorBar.setColor(Color.ofRgb(184, 185, 196).getRgb());
        armorBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_ARMOR);
        armorBar.setForegroundIcon(GuiConstants.ICON_EMPTY);
        armorBar.setAmount(0.75f);

        final UIPropertyBar hungerBar = new UIPropertyBar(this, 75, 9);
        hungerBar.setPosition(0, SimpleScreen.getPaddedY(armorBar, 1), Anchor.CENTER | Anchor.TOP);
        hungerBar.setColor(Color.ofRgb(157, 109, 67).getRgb());
        hungerBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_HUNGER_BACKGROUND);
        hungerBar.setForegroundIcon(GuiConstants.VANILLA_ICON_HUNGER_FOREGROUND);
        hungerBar.setAmount(0.50f);

        final UIPropertyBar airBar = new UIPropertyBar(this, 75, 9);
        airBar.setPosition(0, SimpleScreen.getPaddedY(hungerBar, 1), Anchor.CENTER | Anchor.TOP);
        airBar.setColor(Color.ofRgb(0, 148, 255).getRgb());
        airBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_AIR);
        airBar.setForegroundIcon(GuiConstants.ICON_EMPTY);
        airBar.setAmount(0.25f);

        // Middle container
        this.middleContainer = new UIBackgroundContainer(this, 125, 22);
        this.middleContainer.setPosition(0, -1, Anchor.TOP | Anchor.CENTER);
        this.middleContainer.setPadding(0, 1);
        this.middleContainer.setBackgroundAlpha(180);
        this.middleContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        this.middleContainer.setBorder(Color.ofRgb(70, 70, 70).getRgb(), 1, 200);

        this.worldLabel = new UILabel(this, "");
        this.worldLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        this.compassLabel = new UILabel(this, "");
        this.compassLabel.setPosition(0, -1, Anchor.BOTTOM | Anchor.CENTER);
        this.compassLabel.setFontOptions(FontOptions.builder()
                .from(FontOptionsConstants.FRO_COLOR_WHITE)
                .scale(0.75f)
                .build());

        middleContainer.add(this.worldLabel, this.compassLabel);

        // Right container
        this.rightContainer = new UIBackgroundContainer(this, 125, 22);
        this.rightContainer.setPosition(1, -1, Anchor.TOP | Anchor.RIGHT);
        this.rightContainer.setPadding(1, 1);
        this.rightContainer.setBackgroundAlpha(200);
        this.rightContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        this.rightContainer.setBorder(Color.ofRgb(70, 70, 70).getRgb(), 1, 200);

        this.timeImage = new UIImage(this, new ItemStack(Items.CLOCK));
        this.timeImage.setPosition(-2, 0, Anchor.RIGHT | Anchor.MIDDLE);

        this.playerCountLabel = new UILabel(this, "");
        this.playerCountLabel.setPosition(SimpleScreen.getPaddedX(this.timeImage, 3, Anchor.RIGHT), 1, Anchor.RIGHT | Anchor.MIDDLE);
        this.playerCountLabel.setFontOptions(FontOptions.builder()
                .from(FontOptionsConstants.FRO_COLOR_WHITE)
                .scale(0.50f)
                .build());

        this.playerCountImage = new UIImage(this, new GuiTexture(GuiConstants.AVATAR_GENERIC_LOCATION), null);
        this.playerCountImage.setSize(16, 16);

        this.coordsLabel = new UIExpandingLabel(this, "", true);
        this.coordsLabel.setPosition(2, 3, Anchor.TOP | Anchor.LEFT);
        this.coordsLabel.setFontOptions(FontOptions.builder()
                .from(FontOptionsConstants.FRO_COLOR_WHITE)
                .scale(0.50f)
                .build());

        this.rightContainer.add(this.timeImage, this.playerCountImage, this.playerCountLabel, this.coordsLabel);

        // Survival: Brown
        // Creative: Green
        // Adventure: Blue
        // Spectator: Gray

        tempContainer.add(healthBar, armorBar, hungerBar, airBar);
        addToScreen(tempContainer);
        addToScreen(this.leftContainer);
        addToScreen(this.middleContainer);
        addToScreen(this.rightContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }

        this.compassLabel.setText(HUDData.getCompass());
        this.coordsLabel.setText(
                TextFormatting.GOLD + "X: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getX()) +
                        "\n" +
                        TextFormatting.GOLD + "Y: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getY()) +
                        "\n" +
                        TextFormatting.GOLD + "Z: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getZ()));
        this.currencyLabel.setText(TextFormatting.WHITE + HUDData.PLAYER_CURRENCY);

        if (!Minecraft.getMinecraft().isSingleplayer() || (Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft()
                .getIntegratedServer().getPublic())) {
            this.playerCountImage.setVisible(true);
            this.playerCountLabel.setVisible(true);
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + 1 + "/" + 50);
            this.playerCountLabel.setSize((int) this.playerCountLabel.getFont().getStringWidth(this.playerCountLabel.getText(), this.playerCountLabel
                    .getFontOptions()), (int) this.playerCountLabel.getFont().getStringHeight(this.playerCountLabel.getFontOptions()));
            this.playerCountImage.setPosition(SimpleScreen.getPaddedX(this.playerCountLabel, 2, Anchor.RIGHT), 0, Anchor.RIGHT | Anchor.MIDDLE);
        } else {
            this.playerCountImage.setVisible(false);
            this.playerCountLabel.setVisible(false);
        }

        this.worldLabel.setText(TextFormatting.WHITE + HUDData.WORLD_NAME);
    }

    @Override
    public int getOriginBossBarOffsetY() {
        return 37;
    }

    @Override
    public int getTabMenuOffsetY() {
        return 0;
    }
}
