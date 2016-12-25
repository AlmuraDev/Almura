/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.UILegacyPropertyBar;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LegacyHUD extends AbstractHUD {

    private final UIBackgroundContainer gradientContainer = new UIBackgroundContainer(this);
    private UIImage mapImage, worldImage, playerImage;
    private UILabel worldDisplay;
    private UILabel playerMode;
    private UILabel playerCurrency;
    private UILabel playerTitle;
    private UILabel playerCoords;
    private UILabel serverCount;
    private UILabel playerCompass;
    private UILabel worldTime;
    private UILabel xpLevel;
    private UILegacyPropertyBar healthProperty, armorProperty, hungerProperty, staminaProperty, xpProperty;
    private UIImage clockImage, compassImage;

    @Override
    public void construct() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        gradientContainer.setSize(380, 34);
        gradientContainer.setPosition(0, 0, Anchor.CENTER);
        gradientContainer.setColor(0);
        gradientContainer.setBackgroundAlpha(180);
        gradientContainer.setClipContent(false);

        // ////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        playerTitle = new UILabel(this, mc.player.getDisplayName().getFormattedText());
        playerTitle.setPosition(6, 2, Anchor.LEFT);
        playerTitle.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        // Player Display Mode
        playerMode = new UILabel(this, "");
        playerMode.setPosition(playerTitle.getX() + playerTitle.getText().length() + 4, 2, Anchor.LEFT);
        playerMode.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        // Player Currency
        playerCurrency = new UILabel(this, " ");
        playerCurrency.setPosition(playerMode.getX() + playerMode.getText().length() + 10, 2, Anchor.LEFT);
        playerCurrency.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        // Health Property
        healthProperty = new UILegacyPropertyBar(this, Constants.Gui.LEGACY_ICON_HEART).setPosition(4, 14);

        // Armor Property
        armorProperty = new UILegacyPropertyBar(this, Constants.Gui.LEGACY_ICON_ARMOR).setPosition(4, 24);

        // ////////////////////////////// CENTER COLUMN //////////////////////////////////////

        // Almura Title
        final UILabel almuraTitle = new UILabel(this, "Almura");
        almuraTitle.setPosition(-10, 2, Anchor.TOP | Anchor.CENTER);
        almuraTitle.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        // Hunger Property
        hungerProperty = new UILegacyPropertyBar(this, Constants.Gui.LEGACY_ICON_HUNGER).setPosition(-11, 14, Anchor.CENTER);

        // Stamina Property
        staminaProperty = new UILegacyPropertyBar(this, Constants.Gui.LEGACY_ICON_STAMINA).setPosition(-11, 24, Anchor.CENTER);

        // ////////////////////////////// RIGHT COLUMN //////////////////////////////////////

        // Map Image
        mapImage = new UIImage(this, Constants.Gui.TEXTURE_SPRITESHEET, Constants.Gui.LEGACY_ICON_MAP);
        mapImage.setSize(8, 8);

        // Player Coordinates Label
        playerCoords = new UILabel(this, "x: 0 y: 0 z: 0");
        playerCoords.setPosition(-73, 2, Anchor.RIGHT);
        playerCoords.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        // World Image
        worldImage = new UIImage(this, Constants.Gui.TEXTURE_SPRITESHEET, Constants.Gui.LEGACY_ICON_WORLD);
        worldImage.setSize(8, 8);

        worldDisplay = new UILabel(this, HUDData.WORLD_NAME);
        worldDisplay.setPosition(-5, 2, Anchor.RIGHT);
        worldDisplay.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        // Player Image
        playerImage = new UIImage(this, Constants.Gui.TEXTURE_SPRITESHEET, Constants.Gui.LEGACY_ICON_PLAYER);
        playerImage.setPosition(-125, 14, Anchor.RIGHT);
        playerImage.setSize(8, 8);

        // Player Count Label
        serverCount = new UILabel(this, "--");
        serverCount.setPosition(-110, 14, Anchor.RIGHT);
        serverCount.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        // Player Compass Label
        playerCompass = new UILabel(this, "");
        playerCompass.setPosition(-51, 14, Anchor.RIGHT);
        playerCompass.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        // Compass Image
        compassImage = new UIImage(this, Constants.Gui.TEXTURE_SPRITESHEET, Constants.Gui.LEGACY_ICON_COMPASS);
        compassImage.setPosition(-89, playerCompass.getY() - 1, Anchor.RIGHT);
        compassImage.setSize(8, 8);

        // Clock Image
        clockImage = new UIImage(this, Constants.Gui.TEXTURE_SPRITESHEET, Constants.Gui.LEGACY_ICON_CLOCK);
        clockImage.setSize(8, 8);

        // World Time Label
        worldTime = new UILabel(this, "7pm");
        worldTime.setPosition(-5, 14, Anchor.RIGHT);
        worldTime.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        // XP Property
        xpProperty = new UILegacyPropertyBar(this, Constants.Gui.LEGACY_ICON_XP).setPosition(-27, 24, Anchor.RIGHT);
        xpProperty.setColor(UILegacyPropertyBar.LIGHT_GREEN.getRgb()).setRelativeColor(false);

        // XP Level Label
        xpLevel = new UILabel(this, "1");
        xpLevel.setPosition(-5, 24, Anchor.RIGHT);
        xpLevel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE_SCALE_080);

        gradientContainer
                .add(playerTitle, playerMode, playerCurrency, healthProperty, armorProperty, almuraTitle, hungerProperty, staminaProperty,
                        xpProperty,
                        mapImage, playerCoords, worldImage, worldDisplay, playerImage, serverCount, playerCompass, compassImage, clockImage,
                        worldTime, xpLevel);

        addToScreen(gradientContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {

        // Player Name
        playerTitle.setText(this.mc.player.getDisplayName().getFormattedText());

        // Player Health
        healthProperty.setAmount(this.mc.player.getHealth() / this.mc.player.getMaxHealth());

        // Player Armor
        armorProperty.setAmount(ForgeHooks.getTotalArmorValue(this.mc.player) / 20F);

        // Player Hunger
        hungerProperty.setAmount(this.mc.player.getFoodStats().getFoodLevel() / 20F);

        // Player Stamina
        staminaProperty.setAmount(this.mc.player.getFoodStats().getSaturationLevel() / 20F);

        // Player Experience
        xpProperty.setAmount(this.mc.player.experience);

        // Player Experience level
        xpLevel.setText(Integer.toString(this.mc.player.experienceLevel));

        // Player Mode
        playerMode.setText(this.mc.player.capabilities.isCreativeMode ? "(C)" : "");

        // Server Time
        worldTime.setText(HUDData.getTime());

        // Player Coordinates
        playerCoords.setText(String.format("x: %d y: %d z: %d", (int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ));

        // Player Compass
        playerCompass.setText(HUDData.getCompass());

        // Player Currency
        playerCurrency.setText(HUDData.PLAYER_CURRENCY);

        // World Name
        worldDisplay.setText(HUDData.WORLD_NAME);

        // Player Count
        serverCount.setText(mc.isSingleplayer() ? "--" : HUDData.SERVER_PLAYER_COUNT + "/" + HUDData.SERVER_PLAYER_MAX_COUNT);

        // Alignment
        playerMode.setPosition((playerTitle.getX() + playerTitle.getWidth() + 6), playerMode.getY(), playerMode.getAnchor());
        serverCount
                .setPosition(playerImage.getX() + playerImage.getWidth() + serverCount.getWidth() - 2, serverCount.getY(), serverCount.getAnchor());
        worldImage.setPosition(-(-worldDisplay.getX() + worldDisplay.getWidth() + 2), worldDisplay.getY() - 1, Anchor.RIGHT);
        playerCoords.setPosition(-(-worldImage.getX() + worldImage.getWidth() + 6), playerCoords.getY(), Anchor.RIGHT);
        mapImage.setPosition(-(-playerCoords.getX() + playerCoords.getWidth() + 2), playerCoords.getY() - 1, Anchor.RIGHT);
        clockImage.setPosition(-(-worldTime.getX() + worldTime.getWidth() + 2), worldTime.getY() - 1, Anchor.RIGHT);
        playerMode.setPosition(playerTitle.getX() + playerTitle.getWidth() + 4, 2, Anchor.LEFT);
        playerCurrency.setPosition(playerMode.getX() + playerMode.getText().length() + 4, 2, Anchor.LEFT);
    }

    @Override
    public int getOriginBossBarOffsetY() {
        return 45;
    }

    @Override
    public int getTabMenuOffsetY() {
        return 0;
    }

    @Override
    public int getPotionOffsetY() {
        return 0;
    }
}
