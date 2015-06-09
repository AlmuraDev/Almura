/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIPropertyBar;
import com.almuradev.almurasdk.util.Colors;
import com.almuradev.almurasdk.util.FontRenderOptionsBuilder;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.text.WordUtils;

public class IngameHUD extends SimpleGui {

    private static final String COMPASS_CHARACTERS = "S|.|W|.|N|.|E|.|";
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
    private UIPropertyBar healthProperty, armorProperty, hungerProperty, staminaProperty, xpProperty;

    @Override
    public void construct() {

        // Construct Hud with all elements
        final UIBackgroundContainer gradientContainer = new UIBackgroundContainer(this);
        gradientContainer.setSize(UIComponent.INHERITED, 34);
        gradientContainer.setColor(0);
        gradientContainer.setBackgroundAlpha(180);
        gradientContainer.setClipContent(false);

        // ////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        playerTitle = new UILabel(this, mc.thePlayer.getDisplayName());
        playerTitle.setPosition(6, 2, Anchor.LEFT);
        playerTitle.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);

        // Player Display Mode
        playerMode = new UILabel(this, "");
        playerMode.setPosition(playerTitle.getX() + playerTitle.getText().length() + 4, 2, Anchor.LEFT);
        playerMode.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);

        // Player Currency
        playerCurrency = new UILabel(this, " ");
        playerCurrency.setPosition(playerMode.getX() + playerMode.getText().length() + 4, 2, Anchor.LEFT);
        playerCurrency.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_ORANGE);

        // Health Property
        healthProperty = new UIPropertyBar(this, ICON_HEART).setPosition(5, 14);

        // Armor Property
        armorProperty = new UIPropertyBar(this, ICON_ARMOR).setPosition(5, 24);

        // ////////////////////////////// CENTER COLUMN //////////////////////////////////////

        // Almura Title
        final UILabel almuraTitle = new UILabel(this, "Almura");
        almuraTitle.setPosition(0, 2, Anchor.CENTER);
        almuraTitle.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);

        // Hunger Property
        hungerProperty = new UIPropertyBar(this, ICON_HUNGER).setPosition(-2, 14, Anchor.CENTER);

        // Stamina Property
        staminaProperty = new UIPropertyBar(this, ICON_STAMINA).setPosition(-2, 24, Anchor.CENTER);

        // ////////////////////////////// RIGHT COLUMN //////////////////////////////////////

        // Map Image
        mapImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_MAP);
        mapImage.setPosition(-205, 4, Anchor.RIGHT);
        mapImage.setSize(8, 8);

        // Player Coordinates Label
        playerCoords = new UILabel(this, "x: 0 y: 0 z: 0");
        playerCoords.setPosition(-73, 5, Anchor.RIGHT);
        playerCoords.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        // World Image
        worldImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_WORLD);
        worldImage.setPosition(-45, 4, Anchor.RIGHT);
        worldImage.setSize(8, 8);

        // World Display Label
        String worldName = "World";
        if (mc.isSingleplayer())
            worldName = WordUtils.capitalize(MinecraftServer.getServer().getWorldName());

        worldDisplay = new UILabel(this, worldName);
        worldDisplay.setPosition(-5, 5, Anchor.RIGHT);
        worldDisplay.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        // Player Image
        playerImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_PLAYER);
        playerImage.setPosition(-125, 13, Anchor.RIGHT);
        playerImage.setSize(8, 8);

        // Player Count Label
        serverCount = new UILabel(this, "--");
        serverCount.setPosition(-110, 14, Anchor.RIGHT);
        serverCount.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        // Compass Image
        final UIImage compassImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_COMPASS);
        compassImage.setPosition(-73, 13, Anchor.RIGHT);
        compassImage.setSize(8, 8);

        // Player Compass Label
        playerCompass = new UILabel(this, "");
        playerCompass.setPosition(-51, 14, Anchor.RIGHT);
        playerCompass.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        // Clock Image
        final UIImage clockImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_CLOCK);
        clockImage.setPosition(-25, 12, Anchor.RIGHT);
        clockImage.setSize(7, 7);

        // World Time Label
        worldTime = new UILabel(this, "7pm");
        worldTime.setPosition(-5, 13, Anchor.RIGHT);
        worldTime.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        // XP Property
        xpProperty = new UIPropertyBar(this, ICON_XP).setPosition(-27, 23, Anchor.RIGHT);
        xpProperty.setColor(UIPropertyBar.LIGHT_GREEN).setRelativeColor(false);

        // XP Level Label
        xpLevel = new UILabel(this, "1");
        xpLevel.setPosition(-5, 23, Anchor.RIGHT);
        xpLevel.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8F).build
                ());

        gradientContainer
                .add(playerTitle, playerMode, playerCurrency, healthProperty, armorProperty, almuraTitle, hungerProperty, staminaProperty,
                        xpProperty,
                        mapImage, playerCoords, worldImage, worldDisplay, playerImage, serverCount, playerCompass, compassImage, clockImage,
                        worldTime, xpLevel);

        addToScreen(gradientContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {

        // Player Health
        healthProperty.setAmount(mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth());

        // Player Armor
        armorProperty.setAmount(ForgeHooks.getTotalArmorValue(mc.thePlayer) / 20F);

        // Player Hunger
        hungerProperty.setAmount(getFoodLevel());

        // Player Stamina
        staminaProperty.setAmount(getSaturation());

        // Player Experience
        xpProperty.setAmount(mc.thePlayer.experience);

        // Player Experience level
        xpLevel.setText(Integer.toString(mc.thePlayer.experienceLevel));

        // Player Mode
        playerMode.setText(mc.thePlayer.capabilities.isCreativeMode ? "(C)" : "");

        // Server Time
        worldTime.setText(getTime());

        // Player Coordinates
        playerCoords.setText(String.format("x: %d y: %d z: %d", (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ));

        // Player Compass
        playerCompass.setText(getCompass());

        playerCurrency.setText(HUDData.PLAYER_CURRENCY);

        // World Name (SinglePlayer)
        if (!mc.isSingleplayer()) {
            worldDisplay.setText(HUDData.WORLD_DISPLAY);
        }

        // Player Count
        if (mc.isSingleplayer()) {
            serverCount.setText("--");
        } else {
            serverCount.setText(HUDData.SERVER_COUNT);
        }

        // Alignment
        playerMode.setPosition((playerTitle.getX() + playerTitle.getWidth() + 6), playerMode.getY(), playerMode.getAnchor());
        serverCount
                .setPosition(playerImage.getX() + playerImage.getWidth() + serverCount.getWidth() - 2, serverCount.getY(), serverCount.getAnchor());
        worldImage.setPosition(-(worldDisplay.getWidth() + 9), worldImage.getY(), Anchor.RIGHT);
        playerCoords.setPosition(-(-worldImage.getX() + worldImage.getWidth() + 12), playerCoords.getY(), Anchor.RIGHT);
        mapImage.setPosition(-(-playerCoords.getX() + playerCoords.getWidth() + 6), mapImage.getY(), Anchor.RIGHT);
        playerMode.setPosition(playerTitle.getX() + playerTitle.getWidth() + 4, 2, Anchor.LEFT);
        playerCurrency.setPosition(playerMode.getX() + playerMode.getWidth() + 4, 2, Anchor.LEFT);
    }

    public float getFoodLevel() {
        return mc.thePlayer.getFoodStats().getFoodLevel() / 20F;
    }

    public float getSaturation() {
        return mc.thePlayer.getFoodStats().getSaturationLevel() / 20F;
    }

    public String getCompass() {
        int position = (int) ((((mc.thePlayer.rotationYaw + 11.25) % 360 + 360) % 360) / 360 * 16);

        return "" + Colors.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 3) & 15)
                + Colors.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 2) & 15)
                + Colors.GRAY + COMPASS_CHARACTERS.charAt((position - 1) & 15)
                + Colors.WHITE + COMPASS_CHARACTERS.charAt((position) & 15)
                + Colors.GRAY + COMPASS_CHARACTERS.charAt((position + 1) & 15)
                + Colors.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 2) & 15)
                + Colors.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 3) & 15);
    }

    public String getTime() {
        // Minecraft day is 23000 ticks, we use a 24hr scale, day starts at 6AM
        int hours = (int) (mc.thePlayer.worldObj.getWorldInfo().getWorldTime() / 1000) % 24;

        if (hours >= 0 && hours <= 5) {
            return ((6 + hours) + "am");
        }
        if (hours == 6) {
            return ("12pm");
        }
        if (hours >= 7 && hours <= 17) {
            return ((hours - 6) + "pm");
        }
        if (hours == 18) {
            return ("12am");
        }
        return (hours - 18) + "am";
    }
}
