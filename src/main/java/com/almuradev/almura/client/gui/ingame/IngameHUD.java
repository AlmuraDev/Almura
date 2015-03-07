/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.components.UIPropertyBar;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.util.Color;
import com.almuradev.almurasdk.util.Colors;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Objects;

public class IngameHUD extends SimpleGui {

    private static final Color LIGHT_GREEN = new Color("light_green", 65280);
    private static final Color ORANGE = new Color("orange", 16753920);
    private static final Color LIGHT_ORANGE = new Color("light_orange", 13413376);
    private static final Color RED = new Color("red", 11474462);
    private static final String COMPASS_CHARACTERS = "S|.|W|.|N|.|E|.|";
    public static boolean UPDATES_ENABLED = true;
    public UILabel worldDisplay, playerTitle, playerMode, playerCurrency;
    public UILabel serverCount, almuraTitle;
    private UIImage mapImage, worldImage, playerImage;
    private UILabel playerCoords;
    private UILabel playerCompass;
    private UILabel worldTime;
    private UILabel xpLevel;
    private UIPropertyBar healthProperty, armorProperty, hungerProperty, staminaProperty, xpProperty;

    private float playerHealth, playerArmor, playerHunger, playerStamina, playerExperience, playerExperienceLevel = 0.0F;
    private String serverTime, playerComp, rawWorldName = "";
    private boolean firstPass = true, playerIsCreative = false;
    private int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE, playerCount = Integer.MAX_VALUE;

    public IngameHUD() {
        buildGui();
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void buildGui() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        final UIBackgroundContainer gradientContainer = new UIBackgroundContainer(this);
        gradientContainer.setSize(UIComponent.INHERITED, 34);
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
        playerMode.setPosition(playerTitle.getX() + playerTitle.getText().length() + 4, 2, Anchor.LEFT | Anchor.TOP);
        playerMode.setColor(0xffffffff);
        playerMode.setSize(7, 7);

        // Player Display Name
        playerCurrency = new UILabel(this, " ");
        playerCurrency.setPosition(playerMode.getX() + playerMode.getText().length() + 4, 2, Anchor.LEFT | Anchor.TOP);
        playerCurrency.setColor(ORANGE.getGuiColorCode());
        playerCurrency.setSize(7, 7);

        // Health Property
        healthProperty = new UIPropertyBar(this, TEXTURE_SPRITESHEET, ICON_HEART, ICON_BAR);
        healthProperty.setPosition(5, 14, Anchor.LEFT | Anchor.TOP);
        healthProperty.setSize(105, 7);

        // Armor Property
        armorProperty = new UIPropertyBar(this, TEXTURE_SPRITESHEET, ICON_ARMOR, ICON_BAR);
        armorProperty.setPosition(5, 24, Anchor.LEFT | Anchor.TOP);
        armorProperty.setSize(105, 7);

        //////////////////////////////// CENTER COLUMN //////////////////////////////////////

        // Almura Title
        almuraTitle = new UILabel(this, "Almura");
        almuraTitle.setColor(0xffffffff);
        almuraTitle.setSize(7, 7);
        almuraTitle.setFontScale(1.0F);
        almuraTitle.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);

        // Hunger Property
        hungerProperty = new UIPropertyBar(this, TEXTURE_SPRITESHEET, ICON_HUNGER, ICON_BAR);
        hungerProperty.setPosition(-2, 14, Anchor.CENTER | Anchor.TOP);
        hungerProperty.setSize(105, 7);

        // Stamina Property
        staminaProperty = new UIPropertyBar(this, TEXTURE_SPRITESHEET, ICON_STAMINA, ICON_BAR);
        staminaProperty.setPosition(-2, 24, Anchor.CENTER | Anchor.TOP);
        staminaProperty.setSize(105, 7);

        //////////////////////////////// RIGHT COLUMN //////////////////////////////////////

        // Map Image
        mapImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_MAP);
        mapImage.setPosition(-205, 4, Anchor.RIGHT | Anchor.TOP);
        mapImage.setSize(8, 8);

        // Player Coordinates Label
        playerCoords = new UILabel(this, "x: 0 y: 0 z: 0");
        playerCoords.setPosition(-73, 5, Anchor.RIGHT | Anchor.TOP);
        playerCoords.setColor(0xffffffff);
        playerCoords.setSize(15, 7);
        playerCoords.setFontScale(0.8F);

        // World Image
        worldImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_WORLD);
        worldImage.setPosition(-45, 4, Anchor.RIGHT | Anchor.TOP);
        worldImage.setSize(8, 8);

        // World Display Label
        worldDisplay = new UILabel(this, "World");
        worldDisplay.setPosition(-5, 5, Anchor.RIGHT | Anchor.TOP);
        worldDisplay.setColor(0xffffffff);
        worldDisplay.setSize(15, 7);
        worldDisplay.setFontScale(0.8F);

        // Player Image
        playerImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_PLAYER);
        playerImage.setPosition(-125, 13, Anchor.RIGHT | Anchor.TOP);
        playerImage.setSize(8, 8);

        // Player Count Label
        serverCount = new UILabel(this, "--");
        serverCount.setPosition(-110, 14, Anchor.RIGHT | Anchor.TOP);
        serverCount.setColor(0xffffffff);
        serverCount.setFontScale(0.8F);

        // Compass Image
        final UIImage compassImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_COMPASS);
        compassImage.setPosition(-73, 13, Anchor.RIGHT | Anchor.TOP);
        compassImage.setSize(8, 8);

        // Player Compass Label
        playerCompass = new UILabel(this, "");
        playerCompass.setPosition(-51, 14, Anchor.RIGHT | Anchor.TOP);
        playerCompass.setColor(0xffffffff);
        playerCompass.setSize(35, 7);
        playerCompass.setFontScale(0.8F);

        // Clock Image
        final UIImage clockImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_CLOCK);
        clockImage.setPosition(-25, 12, Anchor.RIGHT | Anchor.TOP);
        clockImage.setSize(7, 7);

        // World Time Label
        worldTime = new UILabel(this, "7pm");
        worldTime.setPosition(-5, 13, Anchor.RIGHT | Anchor.TOP);
        worldTime.setColor(0xffffffff);
        worldTime.setSize(15, 7);
        worldTime.setFontScale(0.8F);

        // XP Property
        xpProperty = new UIPropertyBar(this, TEXTURE_SPRITESHEET, ICON_XP, ICON_BAR);
        xpProperty.setPosition(-27, 23, Anchor.RIGHT | Anchor.TOP);
        xpProperty.setSize(105, 7);
        xpProperty.setColor(LIGHT_GREEN.getGuiColorCode());

        // XP Level Label
        xpLevel = new UILabel(this, "1");
        xpLevel.setPosition(-5, 23, Anchor.RIGHT | Anchor.TOP);
        xpLevel.setColor(0xffffffff);
        xpLevel.setSize(15, 7);
        xpLevel.setFontScale(0.8F);

        gradientContainer
                .add(playerTitle, playerMode, playerCurrency, healthProperty, armorProperty, almuraTitle, hungerProperty, staminaProperty, xpProperty,
                        mapImage, playerCoords, worldImage, worldDisplay, playerImage, serverCount, playerCompass, compassImage, clockImage,
                        worldTime, xpLevel);

        addToScreen(gradientContainer);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (UPDATES_ENABLED && Configuration.DISPLAY_ENHANCED_GUI && mc.thePlayer != null) {
            updateWidgets();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Configuration.DISPLAY_ENHANCED_GUI || !UPDATES_ENABLED) {
            return;
        }

        if (!Configuration.DISPLAY_ENHANCED_DEBUG && Minecraft.getMinecraft().gameSettings.showDebugInfo) {
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
            setWorldAndResolution(mc, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void updateWidgets() {
        if (firstPass) {
            almuraTitle.setColor(0xffffffff);
            almuraTitle.setSize(7, 7);
            almuraTitle.setFontScale(1.0F);
            almuraTitle.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        }

        // Player Health
        if (playerHealth != mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth() || firstPass) {
            playerHealth = mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth();

            if (playerHealth > 0.6) {
                healthProperty.setColor(LIGHT_GREEN.getGuiColorCode());
            } else if (playerHealth >= 0.3) {
                healthProperty.setColor(LIGHT_ORANGE.getGuiColorCode());
            } else {
                healthProperty.setColor(RED.getGuiColorCode());
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
                armorProperty.setColor(LIGHT_GREEN.getGuiColorCode());
            } else if (playerArmor >= 0.3) {
                armorProperty.setColor(LIGHT_ORANGE.getGuiColorCode());
            } else {
                armorProperty.setColor(RED.getGuiColorCode());
            }
            armorProperty.setAmount(playerArmor);
        }

        // Player Hunger
        if (playerHunger != mc.thePlayer.getFoodStats().getFoodLevel() / (float) 20 || firstPass) {
            playerHunger = mc.thePlayer.getFoodStats().getFoodLevel() / (float) 20;
            if (playerHunger > 0) {
                hungerProperty.setAmount(playerHunger);
                hungerProperty.setVisible(true);
            } else {
                hungerProperty.setVisible(false);
            }
            if (playerHunger >= 0.6) {
                hungerProperty.setColor(LIGHT_GREEN.getGuiColorCode());
            } else if (playerHunger >= 0.3) {
                hungerProperty.setColor(LIGHT_ORANGE.getGuiColorCode());
            } else {
                hungerProperty.setColor(RED.getGuiColorCode());
            }
            hungerProperty.setAmount(playerHunger);
        }

        // Player Stamina
        if (playerStamina != mc.thePlayer.getFoodStats().getSaturationLevel() / (float) 20 || firstPass) {
            playerStamina = mc.thePlayer.getFoodStats().getSaturationLevel() / (float) 20;
            if (playerStamina > 0) {
                staminaProperty.setAmount(playerStamina);
                staminaProperty.setVisible(true);
            } else {
                staminaProperty.setVisible(false);
            }

            if (playerStamina >= 0.6) {
                staminaProperty.setColor(LIGHT_GREEN.getGuiColorCode());
            } else if (playerStamina >= 0.3) {
                staminaProperty.setColor(LIGHT_ORANGE.getGuiColorCode());
            } else {
                staminaProperty.setColor(RED.getGuiColorCode());
            }
            staminaProperty.setAmount(playerStamina);
        }

        // Player Experience
        if (playerExperience != mc.thePlayer.experience || firstPass) {
            playerExperience = mc.thePlayer.experience;
            if (playerExperience > 0) {
                xpProperty.setAmount(playerExperience);
                xpProperty.setVisible(true);
            } else {
                xpProperty.setVisible(false);
            }
        }
        // Player Experience level 
        if (playerExperienceLevel != mc.thePlayer.experienceLevel || firstPass) {
            playerExperienceLevel = mc.thePlayer.experienceLevel;
            xpLevel.setText(Integer.toString(mc.thePlayer.experienceLevel));
        }

        // Player Mode
        if (playerIsCreative != mc.thePlayer.capabilities.isCreativeMode || firstPass) {
            playerIsCreative = mc.thePlayer.capabilities.isCreativeMode;
            if (playerIsCreative) {
                playerMode.setText("(C)");
            } else {
                playerMode.setText(" ");
            }
        }

        // Server Time
        if (!Objects.equals(serverTime, getTime()) || firstPass) {
            serverTime = getTime();
            worldTime.setText(getTime());
        }

        boolean dirtyCoords = false;

        if (x == Integer.MIN_VALUE || mc.thePlayer.posX != mc.thePlayer.prevPosX) {
            x = MathHelper.floor_double(mc.thePlayer.posX);
            dirtyCoords = true;
        }

        if (y == Integer.MIN_VALUE || mc.thePlayer.posY != mc.thePlayer.prevPosY) {
            y = MathHelper.floor_double(mc.thePlayer.posY);
            dirtyCoords = true;
        }

        if (z == Integer.MIN_VALUE || mc.thePlayer.posZ != mc.thePlayer.prevPosZ) {
            z = MathHelper.floor_double(mc.thePlayer.posZ);
            dirtyCoords = true;
        }

        // Player Coordinates
        if (dirtyCoords) {
            playerCoords.setText(String.format("x: %d y: %d z: %d", x, y, z));
        }

        // Player Compass
        if (!Objects.equals(playerComp, getCompass()) || firstPass) {
            playerComp = getCompass();
            playerCompass.setText(getCompass());
        }

        // Player Name
        if (playerTitle.getText().isEmpty() && firstPass) {
            playerTitle.setText(mc.thePlayer.getDisplayName());
        }

        //World Name (SinglePlayer)
        if (mc.isSingleplayer()) {
            if (!rawWorldName.equals(MinecraftServer.getServer().getWorldName())) {
                rawWorldName = MinecraftServer.getServer().getWorldName();
                worldDisplay.setText(
                        Character.toUpperCase(rawWorldName.charAt(0)) + rawWorldName.substring(1));
            }
        }

        // Player Count
        if (mc.isSingleplayer() && firstPass) {
            serverCount.setText("--");
        }

        // Alignment
        playerMode.setPosition((playerTitle.getX() + playerTitle.getWidth() + 6), playerMode.getY(), playerMode.getAnchor());
        serverCount
                .setPosition(playerImage.getX() + playerImage.getWidth() + serverCount.getWidth() - 2, serverCount.getY(), serverCount.getAnchor());
        worldImage.setPosition(-(worldDisplay.getWidth() + 9), worldImage.getY(), Anchor.RIGHT | Anchor.TOP);
        playerCoords.setPosition(-(-worldImage.getX() + worldImage.getWidth() + 12), playerCoords.getY(), Anchor.RIGHT | Anchor.TOP);
        mapImage.setPosition(-(-playerCoords.getX() + playerCoords.getWidth() + 6), mapImage.getY(), Anchor.RIGHT | Anchor.TOP);
        playerMode.setPosition(playerTitle.getX() + playerTitle.getWidth() + 4, 2, Anchor.LEFT | Anchor.TOP);
        playerCurrency.setPosition(playerMode.getX() + playerMode.getWidth() + 4, 2, Anchor.LEFT | Anchor.TOP);
        firstPass = false;
    }

    public float getArmorLevel() {
        int armorDamage = 0;
        int armorTotal = 0;
        final ItemStack[] inv = mc.thePlayer.inventory.armorInventory;

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
        //Minecraft day is 23000 ticks, we use a 24hr scale, day starts at 6AM
        int hours = (int) (mc.thePlayer.worldObj.getWorldInfo().getWorldTime() / 1000) % 24;

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
