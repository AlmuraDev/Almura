/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.UIPropertyBar;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.awt.*;

public class IngameHUD extends MalisisGui {

    public static final GuiTexture BAR_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/barwidget.png"));
    public static final GuiTexture HEART_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/health.png"));
    public static final GuiTexture ARMOR_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/armor.png"));
    public static final GuiTexture HUNGER_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/hunger.png"));
    public static final GuiTexture STAMINA_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/stamina.png"));
    public static final GuiTexture XP_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/xp.png"));
    public static final GuiTexture PLAYER_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/player.png"));
    public static final GuiTexture COMPASS_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/compass.png"));
    public static final GuiTexture MAP_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/map.png"));
    public static final GuiTexture WORLD_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/world.png"));
    public static final GuiTexture CLOCK_TEXTURE = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/clock.png"));

    private static final Color greenBar = new Color(0, 1f, 0, 1f);
    private static final Color orangeBar = new Color(0.8039f, 0.6784f, 0f, 1f);
    private static final Color redBar = new Color(0.69f, 0.09f, 0.12f, 1f);

    private final UIBackgroundContainer gradientContainer;
    private final UIImage mapImage, worldImage;
    private final UILabel almuraTitle, playerTitle, serverCount, playerCoords, playerCompass, worldDisplay, worldTime, xpLevel;
    private final UIPropertyBar healthProperty, armorProperty, hungerProperty, staminaProperty, xpProperty;

    @SuppressWarnings("rawtypes")
    public IngameHUD() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        gradientContainer = new UIBackgroundContainer(this);
        gradientContainer.setSize(UIComponent.INHERITED, 40);
        gradientContainer.setColor(Integer.MIN_VALUE);
        gradientContainer.setTopAlpha(180);
        gradientContainer.setBottomAlpha(180);

        // Player Display Name
        playerTitle = new UILabel(this, "PlayerName");
        playerTitle.setPosition(6, 2, Anchor.LEFT | Anchor.TOP);
        playerTitle.setColor(0xffffffff);
        playerTitle.setSize(7, 7);

        //////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Health Property
        healthProperty = new UIPropertyBar(this, HEART_TEXTURE, BAR_TEXTURE);
        healthProperty.setPosition(5, 17, Anchor.LEFT | Anchor.TOP);
        healthProperty.setSize(118, 7);

        // Armor Property
        armorProperty = new UIPropertyBar(this, ARMOR_TEXTURE, BAR_TEXTURE);
        armorProperty.setPosition(5, 28, Anchor.LEFT | Anchor.TOP);
        armorProperty.setSize(118, 7);

        //////////////////////////////// CENTER COLUMN //////////////////////////////////////

        // Almura Title
        almuraTitle = new UILabel(this, "Almura");
        almuraTitle.setPosition(-15, 2, Anchor.CENTER | Anchor.TOP);
        almuraTitle.setColor(0xffffffff);
        almuraTitle.setSize(7, 7);
        almuraTitle.setFontScale(1.2F);

        // Hunger Property
        hungerProperty = new UIPropertyBar(this, HUNGER_TEXTURE, BAR_TEXTURE);
        hungerProperty.setPosition(-2, 17, Anchor.CENTER | Anchor.TOP);
        hungerProperty.setSize(118, 7);

        // Stamina Property
        staminaProperty = new UIPropertyBar(this, STAMINA_TEXTURE, BAR_TEXTURE);
        staminaProperty.setPosition(-2, 28, Anchor.CENTER | Anchor.TOP);
        staminaProperty.setSize(118, 7);

        //////////////////////////////// RIGHT COLUMN //////////////////////////////////////

        // Map Image
        mapImage = new UIImage(this, MAP_TEXTURE, null);
        mapImage.setPosition(-205, 5, Anchor.RIGHT | Anchor.TOP);
        mapImage.setSize(8, 8);

        // Player Coordinates Label
        playerCoords = new UILabel(this, "x: 0 y: 0 z: 0");
        playerCoords.setPosition(-73, 5, Anchor.RIGHT | Anchor.TOP);
        playerCoords.setColor(0xffffffff);
        playerCoords.setSize(15, 7);
        playerCoords.setFontScale(0.8F);

        // World Image
        worldImage = new UIImage(this, WORLD_TEXTURE, null);
        worldImage.setPosition(-45, 5, Anchor.RIGHT | Anchor.TOP);
        worldImage.setSize(8, 8);

        // World Display Label
        worldDisplay = new UILabel(this, "WORLD");
        worldDisplay.setPosition(-5, 5, Anchor.RIGHT | Anchor.TOP);
        worldDisplay.setColor(0xffffffff);
        worldDisplay.setSize(15, 7);
        worldDisplay.setFontScale(0.8F);

        // Player Image
        final UIImage playerImage = new UIImage(this, PLAYER_TEXTURE, null);
        playerImage.setPosition(-115, 16, Anchor.RIGHT | Anchor.TOP);
        playerImage.setSize(8, 8);

        // Player Count Label
        serverCount = new UILabel(this, "0 / 50");
        serverCount.setPosition(-95, 17, Anchor.RIGHT | Anchor.TOP);
        serverCount.setColor(0xffffffff);
        serverCount.setSize(15, 7);
        serverCount.setFontScale(0.8F);

        // Compass Image
        final UIImage compassImage = new UIImage(this, COMPASS_TEXTURE, null);
        compassImage.setPosition(-67, 16, Anchor.RIGHT | Anchor.TOP);
        compassImage.setSize(8, 8);

        // Player Compass Label
        playerCompass = new UILabel(this, "");
        playerCompass.setPosition(-47, 17, Anchor.RIGHT | Anchor.TOP);
        playerCompass.setColor(0xffffffff);
        playerCompass.setSize(35, 7);
        playerCompass.setFontScale(0.8F);

        // Clock Image
        final UIImage clockImage = new UIImage(this, CLOCK_TEXTURE, null);
        clockImage.setPosition(-25, 16, Anchor.RIGHT | Anchor.TOP);
        clockImage.setSize(7, 7);

        // World Time Label
        worldTime = new UILabel(this, "7pm");
        worldTime.setPosition(-5, 16, Anchor.RIGHT | Anchor.TOP);
        worldTime.setColor(0xffffffff);
        worldTime.setSize(15, 7);
        worldTime.setFontScale(0.8F);

        // XP Property
        xpProperty = new UIPropertyBar(this, XP_TEXTURE, BAR_TEXTURE);
        xpProperty.setPosition(-25, 28, Anchor.RIGHT | Anchor.TOP);
        xpProperty.setSize(118, 7);

        // XP Level Label
        xpLevel = new UILabel(this, "1");
        xpLevel.setPosition(-15, 27, Anchor.RIGHT | Anchor.TOP);
        xpLevel.setColor(0xffffffff);
        xpLevel.setSize(15, 7);
        xpLevel.setFontScale(0.8F);

        gradientContainer.add(playerTitle);
        gradientContainer.add(healthProperty);
        gradientContainer.add(armorProperty);
        gradientContainer.add(almuraTitle);
        gradientContainer.add(hungerProperty);
        gradientContainer.add(staminaProperty);
        gradientContainer.add(xpProperty);
        gradientContainer.add(mapImage);
        gradientContainer.add(playerCoords);
        gradientContainer.add(worldImage);
        gradientContainer.add(worldDisplay);
        gradientContainer.add(playerImage);
        gradientContainer.add(serverCount);
        gradientContainer.add(playerCompass);
        gradientContainer.add(compassImage);
        gradientContainer.add(clockImage);
        gradientContainer.add(worldTime);
        gradientContainer.add(xpLevel);

        addToScreen(gradientContainer);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        switch (event.type) {
            case HEALTH:
            case ARMOR:
            case FOOD:
            case EXPERIENCE:
                event.setCanceled(true);
        }

        if (event.type == RenderGameOverlayEvent.ElementType.HEALTH) {
            setWorldAndResolution(Minecraft.getMinecraft(), event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            updateWidgets();
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void updateWidgets() {
        int playerHealth = Math.max(0, Math.min(100, (int) (Minecraft.getMinecraft().thePlayer.getHealth() * 5)));
        int playerArmor = getArmorLevel();
        int playerHunger = Math.max(0, Math.min(100, (Minecraft.getMinecraft().thePlayer.getFoodStats().getFoodLevel() * 5)));
        int playerStamina = (int) Math.max(0, Math.min(100, (Minecraft.getMinecraft().thePlayer.getFoodStats().getSaturationLevel() * 5)));
        int currentExp = Math.max(0, Math.min(100, (int) (Minecraft.getMinecraft().thePlayer.experience * 100)));
        healthProperty.setAmount(playerHealth);
        hungerProperty.setAmount(playerHunger);
        staminaProperty.setAmount(playerStamina);

        xpProperty.setColor(greenBar.getRGB());

        if (playerHealth > 66) {
            healthProperty.setColor(greenBar.getRGB());
        } else if (playerHealth >= 33) {
            healthProperty.setColor(orangeBar.getRGB());
        } else {
            healthProperty.setColor(redBar.getRGB());
        }

        if (playerHunger > 66) {
            hungerProperty.setColor(greenBar.getRGB());
        } else if (playerArmor >= 33) {
            hungerProperty.setColor(orangeBar.getRGB());
        } else {
            hungerProperty.setColor(redBar.getRGB());
        }

        if (playerStamina > 0) {
            staminaProperty.setAmount(playerStamina);
            staminaProperty.setVisible(true);
        } else {
            staminaProperty.setVisible(false);
        }

        if (playerStamina > 66) {
            staminaProperty.setColor(greenBar.getRGB());
        } else if (playerArmor >= 33) {
            staminaProperty.setColor(orangeBar.getRGB());
        } else {
            staminaProperty.setColor(redBar.getRGB());
        }

        if (playerArmor > 0) {
            armorProperty.setAmount(playerArmor);
            armorProperty.setVisible(true);
        } else {
            armorProperty.setVisible(false);
        }

        if (playerArmor > 66) {
            armorProperty.setColor(greenBar.getRGB());
        } else if (playerArmor >= 33) {
            armorProperty.setColor(orangeBar.getRGB());
        } else {
            armorProperty.setColor(redBar.getRGB());
        }

        if (currentExp > 0) {
            xpProperty.setAmount(currentExp);
            xpProperty.setVisible(true);
        } else {
            xpProperty.setVisible(false);
        }

        serverCount.setText(MinecraftServer.getServer().getCurrentPlayerCount() + "/" + MinecraftServer.getServer().getMaxPlayers());
        playerTitle.setText(Minecraft.getMinecraft().thePlayer.getDisplayName());
        playerCoords.setText(
                String.format("x: %d,  y: %d,  z: %d", (int) Minecraft.getMinecraft().thePlayer.posX, (int) Minecraft.getMinecraft().thePlayer.posY,
                              (int) Minecraft.getMinecraft().thePlayer.posZ));
        playerCompass.setText(getCompass());
        mapImage.setPosition(-(playerCoords.getWidth() + 80), 4, Anchor.RIGHT | Anchor.TOP);
        worldDisplay.setText(MinecraftServer.getServer().getWorldName());
        worldImage.setPosition(-(worldDisplay.getWidth() + 9), 4);
        worldTime.setText(getTime());
        xpLevel.setText(Integer.toString(Minecraft.getMinecraft().thePlayer.experienceLevel));
    }

    public int getArmorLevel() {
        int armor = 0;
        int armorTotal = 0;
        final net.minecraft.item.ItemStack[] inv = Minecraft.getMinecraft().thePlayer.inventory.armorInventory;

        if (inv != null) {
            for (final ItemStack armorItem : inv) {
                if (armorItem != null) {
                    int max = armorItem.getMaxDamage();
                    armorTotal = armorTotal + max;
                    int current = armorItem.getItemDamage();
                    armor = armor + current;
                }
            }
        }

        if (armorTotal != 0 && armor != 0) {
            double par1 = armorTotal - armor;
            double par2 = par1 / armorTotal;
            double par3 = par2 * 100;
            return ((int) par3);
        } else {
            return 0;
        }
    }

    public String getCompass() {
        int angle = (int) (((Minecraft.getMinecraft().thePlayer.rotationYaw + 360 + 11.25) / 22.5) % 16) + 3;
        // String dirs = "|.|N|.|E|.|S|.|W|.|N|.";
        String dirs = "|.|S|.|W|.|N|.|E|.|S|."; //Match the compass to the in-game compass.
        if (angle >= 2) {
            try {
                return "" + ChatColor.DARK_GRAY + dirs.charAt(angle - 3)
                       + ChatColor.DARK_GRAY + dirs.charAt(angle - 2)
                       + ChatColor.GRAY + dirs.charAt(angle - 1)
                       + ChatColor.WHITE + dirs.charAt(angle)
                       + ChatColor.GRAY + dirs.charAt(angle + 1)
                       + ChatColor.DARK_GRAY + dirs.charAt(angle + 2)
                       + ChatColor.DARK_GRAY + dirs.charAt(angle + 3);
            } catch (Exception e) {
                //ignore exception being thrown for some dumb reason.
            }
        }
        return "";
    }

    public String getTime() {
        int hours = (int) ((Minecraft.getMinecraft().thePlayer.worldObj.getWorldInfo().getWorldTime() / 1000L + 6L) % 24L);
        if (hours == 1) {
            return (String.format("12am"));
        }
        if (hours > 1 && hours <= 11) {
            return (String.format(hours + "am"));
        }
        if (hours == 12) {
            return (String.format(hours + "pm"));
        }
        if (hours >= 13) {
            return (String.format((hours - 12) + "pm"));
        }
        return "0pm";
    }
}
