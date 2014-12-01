/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.client.gui.UIPropertyBar;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

public class IngameHUD extends AlmuraGui {

    private static final GuiIcon ICON_BAR = getIcon(0, 126, 256, 14);
    private static final GuiIcon ICON_HEART = getIcon(149, 62, 26, 26);
    private static final GuiIcon ICON_ARMOR = getIcon(64, 63, 20, 27);
    private static final GuiIcon ICON_HUNGER = getIcon(198, 96, 28, 29);
    private static final GuiIcon ICON_STAMINA = getIcon(99, 93, 32, 31);
    private static final GuiIcon ICON_XP = getIcon(169, 98, 24, 24);
    private static final GuiIcon ICON_PLAYER = getIcon(67, 92, 28, 32);
    private static final GuiIcon ICON_COMPASS = getIcon(118, 66, 30, 26);
    private static final GuiIcon ICON_MAP = getIcon(0, 95, 32, 26);
    private static final GuiIcon ICON_WORLD = getIcon(133, 93, 32, 32);
    private static final GuiIcon ICON_CLOCK = getIcon(86, 64, 28, 26);

    private static final String COMPASS_CHARACTERS = "S|.|W|.|N|.|E|.|";
    private static final Color greenBar = new Color(0f, 1f, 0f, 1f);
    private static final Color orangeBar = new Color(0.8039f, 0.6784f, 0f, 1f);
    private static final Color redBar = new Color(0.69f, 0.09f, 0.12f, 1f);
    public static IngameHUD INSTANCE;
    public static boolean UPDATES_ENABLED = false;
    public final UILabel worldDisplay, playerTitle, playerMode;
    private final UIImage mapImage, worldImage, playerImage;
    private final UILabel serverCount, playerCoords, playerCompass, worldTime, xpLevel;
    private final UIPropertyBar healthProperty, armorProperty, hungerProperty, staminaProperty, xpProperty;

    private float playerHealth, playerArmor, playerHunger, playerStamina, playerExperience, playerExperienceLevel = 0.0F;
    private String serverTime, playerLocation, playerComp;
    private boolean firstPass, playerIsCreative;

    @SuppressWarnings("rawtypes")
    public IngameHUD() {
        INSTANCE = this;
        guiscreenBackground = false;
        firstPass = true;
        playerIsCreative = false;

        // Construct Hud with all elements
        final UIBackgroundContainer gradientContainer = new UIBackgroundContainer(this);
        gradientContainer.setSize(UIComponent.INHERITED, 40);
        gradientContainer.setColor(Integer.MIN_VALUE);
        gradientContainer.setTopAlpha(180);
        gradientContainer.setBottomAlpha(180);
        gradientContainer.setClipContent(false);

        //////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        playerTitle = new UILabel(this, "");
        playerTitle.setPosition(6, 2, Anchor.LEFT | Anchor.TOP);
        playerTitle.setColor(0xffffffff);
        playerTitle.setSize(7, 7);

        // Player Display Mode
        playerMode = new UILabel(this, "");
        playerMode.setPosition(25, 2, Anchor.LEFT | Anchor.TOP);
        playerMode.setColor(0xffffffff);
        playerMode.setSize(7, 7);

        // Health Property
        healthProperty = new UIPropertyBar(this, TEXTURE_DEFAULT, ICON_HEART, ICON_BAR);
        healthProperty.setPosition(5, 17, Anchor.LEFT | Anchor.TOP);
        healthProperty.setSize(105, 7);

        // Armor Property
        armorProperty = new UIPropertyBar(this, TEXTURE_DEFAULT, ICON_ARMOR, ICON_BAR);
        armorProperty.setPosition(5, 28, Anchor.LEFT | Anchor.TOP);
        armorProperty.setSize(105, 7);

        //////////////////////////////// CENTER COLUMN //////////////////////////////////////

        // Almura Title
        final UILabel almuraTitle = new UILabel(this, "Almura");
        almuraTitle.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        almuraTitle.setColor(0xffffffff);
        almuraTitle.setSize(7, 7);
        almuraTitle.setFontScale(1.0F);

        // Hunger Property
        hungerProperty = new UIPropertyBar(this, TEXTURE_DEFAULT, ICON_HUNGER, ICON_BAR);
        hungerProperty.setPosition(-2, 17, Anchor.CENTER | Anchor.TOP);
        hungerProperty.setSize(105, 7);

        // Stamina Property
        staminaProperty = new UIPropertyBar(this, TEXTURE_DEFAULT, ICON_STAMINA, ICON_BAR);
        staminaProperty.setPosition(-2, 28, Anchor.CENTER | Anchor.TOP);
        staminaProperty.setSize(105, 7);

        //////////////////////////////// RIGHT COLUMN //////////////////////////////////////

        // Map Image
        mapImage = new UIImage(this, TEXTURE_DEFAULT, ICON_MAP);
        mapImage.setPosition(-205, 4, Anchor.RIGHT | Anchor.TOP);
        mapImage.setSize(8, 8);

        // Player Coordinates Label
        playerCoords = new UILabel(this, "x: 0 y: 0 z: 0");
        playerCoords.setPosition(-73, 5, Anchor.RIGHT | Anchor.TOP);
        playerCoords.setColor(0xffffffff);
        playerCoords.setSize(15, 7);
        playerCoords.setFontScale(0.8F);

        // World Image
        worldImage = new UIImage(this, TEXTURE_DEFAULT, ICON_WORLD);
        worldImage.setPosition(-45, 4, Anchor.RIGHT | Anchor.TOP);
        worldImage.setSize(8, 8);

        // World Display Label
        worldDisplay = new UILabel(this, "WORLD");
        worldDisplay.setPosition(-5, 5, Anchor.RIGHT | Anchor.TOP);
        worldDisplay.setColor(0xffffffff);
        worldDisplay.setSize(15, 7);
        worldDisplay.setFontScale(0.8F);

        // Player Image
        playerImage = new UIImage(this, TEXTURE_DEFAULT, ICON_PLAYER);
        playerImage.setPosition(-125, 16, Anchor.RIGHT | Anchor.TOP);
        playerImage.setSize(8, 8);

        // Player Count Label
        serverCount = new UILabel(this, "--");
        serverCount.setPosition(-110, 17, Anchor.RIGHT | Anchor.TOP);
        serverCount.setColor(0xffffffff);
        serverCount.setFontScale(0.8F);

        // Compass Image
        final UIImage compassImage = new UIImage(this, TEXTURE_DEFAULT, ICON_COMPASS);
        compassImage.setPosition(-73, 16, Anchor.RIGHT | Anchor.TOP);
        compassImage.setSize(8, 8);

        // Player Compass Label
        playerCompass = new UILabel(this, "");
        playerCompass.setPosition(-51, 17, Anchor.RIGHT | Anchor.TOP);
        playerCompass.setColor(0xffffffff);
        playerCompass.setSize(35, 7);
        playerCompass.setFontScale(0.8F);

        // Clock Image
        final UIImage clockImage = new UIImage(this, TEXTURE_DEFAULT, ICON_CLOCK);
        clockImage.setPosition(-25, 15, Anchor.RIGHT | Anchor.TOP);
        clockImage.setSize(7, 7);

        // World Time Label
        worldTime = new UILabel(this, "7pm");
        worldTime.setPosition(-5, 16, Anchor.RIGHT | Anchor.TOP);
        worldTime.setColor(0xffffffff);
        worldTime.setSize(15, 7);
        worldTime.setFontScale(0.8F);

        // XP Property
        xpProperty = new UIPropertyBar(this, TEXTURE_DEFAULT, ICON_XP, ICON_BAR);
        xpProperty.setPosition(-27, 28, Anchor.RIGHT | Anchor.TOP);
        xpProperty.setSize(105, 7);
        xpProperty.setColor(greenBar.getRGB());

        // XP Level Label
        xpLevel = new UILabel(this, "1");
        xpLevel.setPosition(-5, 28, Anchor.RIGHT | Anchor.TOP);
        xpLevel.setColor(0xffffffff);
        xpLevel.setSize(15, 7);
        xpLevel.setFontScale(0.8F);

        gradientContainer.add(playerTitle, playerMode, healthProperty, armorProperty, almuraTitle, hungerProperty, staminaProperty, xpProperty,
                              mapImage, playerCoords, worldImage, worldDisplay, playerImage, serverCount, playerCompass, compassImage, clockImage,
                              worldTime, xpLevel);

        addToScreen(gradientContainer);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (UPDATES_ENABLED && Minecraft.getMinecraft().thePlayer != null && Configuration.ALMURA_GUI) {
            updateWidgets();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Configuration.ALMURA_GUI) {
            return;
        }
        switch (event.type) {
            case HEALTH:
            case ARMOR:
            case FOOD:
            case EXPERIENCE:
                event.setCanceled(true);
        }

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            UPDATES_ENABLED = true;
            setWorldAndResolution(Minecraft.getMinecraft(), event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }


    public void updateWidgets() {
        // Player Health
        if (playerHealth != Minecraft.getMinecraft().thePlayer.getHealth() / Minecraft.getMinecraft().thePlayer.getMaxHealth() || firstPass) {
            playerHealth = Minecraft.getMinecraft().thePlayer.getHealth() / Minecraft.getMinecraft().thePlayer.getMaxHealth();
            if (playerHealth > 0.6) {
                healthProperty.setColor(greenBar.getRGB());
            } else if (playerHealth >= 0.3) {
                healthProperty.setColor(orangeBar.getRGB());
            } else {
                healthProperty.setColor(redBar.getRGB());
            }
            healthProperty.setAmount(playerHealth);
        }
        // Player Armor
        if (playerArmor != getArmorLevel() || firstPass) {
            playerArmor = getArmorLevel();
            if (playerArmor > 0) {
                armorProperty.setAmount(playerArmor);
                armorProperty.setVisible(true);
            } else {
                armorProperty.setVisible(false);
            }
            if (playerArmor > 0.6) {
                armorProperty.setColor(greenBar.getRGB());
            } else if (playerArmor >= 0.3) {
                armorProperty.setColor(orangeBar.getRGB());
            } else {
                armorProperty.setColor(redBar.getRGB());
            }
            armorProperty.setAmount(playerArmor);
        }
        // Player Hunger
        if (playerHunger != Minecraft.getMinecraft().thePlayer.getFoodStats().getFoodLevel() / (float) 20 || firstPass) {
            playerHunger = Minecraft.getMinecraft().thePlayer.getFoodStats().getFoodLevel() / (float) 20;
            if (playerHunger > 0) {
                hungerProperty.setAmount(playerHunger);
                hungerProperty.setVisible(true);
            } else {
                hungerProperty.setVisible(false);
            }
            if (playerHunger >= 0.6) {
                hungerProperty.setColor(greenBar.getRGB());
            } else if (playerHunger >= 0.3) {
                hungerProperty.setColor(orangeBar.getRGB());
            } else {
                hungerProperty.setColor(redBar.getRGB());
            }
            hungerProperty.setAmount(playerHunger);
        }
        // Player Stamina
        if (playerStamina != Minecraft.getMinecraft().thePlayer.getFoodStats().getSaturationLevel() / (float) 20 || firstPass) {
            playerStamina = Minecraft.getMinecraft().thePlayer.getFoodStats().getSaturationLevel() / (float) 20;
            if (playerStamina > 0) {
                staminaProperty.setAmount(playerStamina);
                staminaProperty.setVisible(true);
            } else {
                staminaProperty.setVisible(false);
            }

            if (playerStamina >= 0.6) {
                staminaProperty.setColor(greenBar.getRGB());
            } else if (playerStamina >= 0.3) {
                staminaProperty.setColor(orangeBar.getRGB());
            } else {
                staminaProperty.setColor(redBar.getRGB());
            }
            staminaProperty.setAmount(playerStamina);
        }
        // Player Experience
        if (playerExperience != Minecraft.getMinecraft().thePlayer.experience || firstPass) {
            playerExperience = Minecraft.getMinecraft().thePlayer.experience;
            if (playerExperience > 0) {
                xpProperty.setAmount(playerExperience);
                xpProperty.setVisible(true);
            } else {
                xpProperty.setVisible(false);
            }
        }
        // Player Experience level 
        if (playerExperienceLevel != Minecraft.getMinecraft().thePlayer.experienceLevel || firstPass) {
            playerExperienceLevel = Minecraft.getMinecraft().thePlayer.experienceLevel;
            xpLevel.setText(Integer.toString(Minecraft.getMinecraft().thePlayer.experienceLevel));
        }
        // Player Mode
        if (playerIsCreative != Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode || firstPass) {
            if (playerIsCreative) {
                playerMode.setText("(C)");
            } else {
                playerMode.setText("");
            }
        }
        // Server Time
        if (!Objects.equals(serverTime, getTime()) || firstPass) {
            serverTime = getTime();
            worldTime.setText(getTime());
        }
        // Player Coordinates
        if (!Objects.equals(playerLocation, String.format("x: %d y: %d z: %d", (int) Minecraft.getMinecraft().thePlayer.posX,
                                                          (int) Minecraft.getMinecraft().thePlayer.posY,
                                                          (int) Minecraft.getMinecraft().thePlayer.posZ))
            || firstPass) {
            playerLocation =
                    String.format("x: %d y: %d z: %d", (int) Minecraft.getMinecraft().thePlayer.posX, (int) Minecraft.getMinecraft().thePlayer.posY,
                                  (int) Minecraft.getMinecraft().thePlayer.posZ);
            playerCoords.setText(
                    String.format("x: %d y: %d z: %d", (int) Minecraft.getMinecraft().thePlayer.posX, (int) Minecraft.getMinecraft().thePlayer.posY,
                                  (int) Minecraft.getMinecraft().thePlayer.posZ));
        }
        // Player Compass
        if (!Objects.equals(playerComp, getCompass()) || firstPass) {
            playerComp = getCompass();
            playerCompass.setText(getCompass());
        }
        // Player Name
        if (playerTitle.getText().isEmpty()) {
            playerTitle.setText(Minecraft.getMinecraft().thePlayer.getDisplayName());
        }
        if (Minecraft.getMinecraft().isSingleplayer()) {
            worldDisplay.setText(
                    Character.toUpperCase(MinecraftServer.getServer().getWorldName().charAt(0)) + MinecraftServer.getServer().getWorldName()
                            .substring(
                                    1));
        }
        // Server Player Count
        if (Minecraft.getMinecraft().isSingleplayer()) {
            serverCount.setText("--");
        } else {
            serverCount.setText(Minecraft.getMinecraft().getNetHandler().playerInfoList.size() + "/" + Minecraft.getMinecraft()
                    .getNetHandler().currentServerMaxPlayers);
        }
        // Alignment
        playerMode.setPosition((playerTitle.getX() + playerTitle.getWidth() + 6), playerMode.getY(), playerMode.getAnchor());
        serverCount
                .setPosition(playerImage.getX() + playerImage.getWidth() + serverCount.getWidth() - 2, serverCount.getY(), serverCount.getAnchor());
        worldImage.setPosition(-(worldDisplay.getWidth() + 9), worldImage.getY(), Anchor.RIGHT | Anchor.TOP);
        playerCoords.setPosition(-(-worldImage.getX() + worldImage.getWidth() + 12), playerCoords.getY(), Anchor.RIGHT | Anchor.TOP);
        mapImage.setPosition(-(-playerCoords.getX() + playerCoords.getWidth() + 6), mapImage.getY(), Anchor.RIGHT | Anchor.TOP);
        firstPass = false;
    }

    public float getArmorLevel() {
        int armorDamage = 0;
        int armorTotal = 0;
        final ItemStack[] inv = Minecraft.getMinecraft().thePlayer.inventory.armorInventory;

        if (inv != null) {
            for (ItemStack armorItem : inv) {
                if (armorItem != null) {
                    armorTotal += armorItem.getMaxDamage();
                    armorDamage += armorItem.getItemDamage();
                }
            }
        }

        if (armorTotal == 0) {
            return 0;
        }

        return (armorTotal - armorDamage) / (float) armorTotal;
    }

    public String getCompass() {
        int position = (int) ((((Minecraft.getMinecraft().thePlayer.rotationYaw + 11.25) % 360 + 360) % 360) / 360 * 16);

        return "" + ChatColor.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 3) & 15)
               + ChatColor.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 2) & 15)
               + ChatColor.GRAY + COMPASS_CHARACTERS.charAt((position - 1) & 15)
               + ChatColor.WHITE + COMPASS_CHARACTERS.charAt((position) & 15)
               + ChatColor.GRAY + COMPASS_CHARACTERS.charAt((position + 1) & 15)
               + ChatColor.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 2) & 15)
               + ChatColor.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 3) & 15);
    }

    public String getTime() {
        //Minecraft day is 23000 ticks, we use a 24hr scale, day starts at 6AM
        int hours = (int) (Minecraft.getMinecraft().thePlayer.worldObj.getWorldInfo().getWorldTime() / 1000) % 24;

        if (hours >= 0 && hours <= 5) {
            return (String.format((6 + hours) + "am"));
        }
        if (hours == 6) {
            return (String.format("12pm"));
        }
        if (hours >= 7 && hours <= 17) {
            return (String.format((hours - 6) + "pm"));
        }
        if (hours == 18) {
            return (String.format("12am"));
        }
        return String.format((hours - 18) + "am");
    }
}
