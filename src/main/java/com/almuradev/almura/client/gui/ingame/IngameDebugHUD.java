/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

public class IngameDebugHUD extends AlmuraGui {

    public static boolean UPDATES_ENABLED = false;
    public static final Runtime RUNTIME = Runtime.getRuntime();
    public UILabel fps, memoryDebug, memoryAllocated, xLoc, yLoc, zLoc, directionLoc, biomeName, blockLight, skyLight, rawLight;
    private Chunk chunk;
    private int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE, yaw = Integer.MIN_VALUE;

    public IngameDebugHUD() {
        super(null);
        setup();
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void setup() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        final UIBackgroundContainer debugPanel = new UIBackgroundContainer(this);
        debugPanel.setSize(285, 175);
        debugPanel.setPosition(5, 0, Anchor.LEFT | Anchor.MIDDLE);
        debugPanel.setColor(Integer.MIN_VALUE);
        debugPanel.setTopAlpha(180);
        debugPanel.setBottomAlpha(180);
        debugPanel.setClipContent(false);

        // Title
        UILabel debugTitle = new UILabel(this, ChatColor.AQUA + "Almura Debug");
        debugTitle.setPosition(0, 3, Anchor.CENTER | Anchor.TOP);
        debugTitle.setFontScale(1.1F);

        // FPS
        UILabel fpsLabel = new UILabel(this, ChatColor.WHITE + "FPS: ");
        fpsLabel.setPosition(5, 20, Anchor.LEFT | Anchor.TOP);
        fps = new UILabel(this, "fpsDrawsHere");
        fps.setPosition(32, 20, Anchor.LEFT | Anchor.TOP);

        // MC Memory Usage
        UILabel memoryLabel = new UILabel(this, ChatColor.WHITE + "Memory: ");
        memoryLabel.setPosition(5, 30, Anchor.LEFT | Anchor.TOP);
        memoryDebug = new UILabel(this, ChatColor.GRAY + "memoryDebug");
        memoryDebug.setPosition(60, 30, Anchor.LEFT | Anchor.TOP);
        memoryAllocated = new UILabel(this, ChatColor.GRAY + "memoryDebug");
        memoryAllocated.setPosition(60, 40, Anchor.LEFT | Anchor.TOP);

        // Location Stats
        UILabel directionLabel = new UILabel(this, ChatColor.WHITE + "Directions:");
        directionLabel.setPosition(5, 50, Anchor.LEFT | Anchor.TOP);

        xLoc = new UILabel(this, ChatColor.GRAY + "");
        xLoc.setPosition(60, 60, Anchor.LEFT | Anchor.TOP);
        yLoc = new UILabel(this, ChatColor.GRAY + "");
        yLoc.setPosition(60, 70, Anchor.LEFT | Anchor.TOP);
        zLoc = new UILabel(this, ChatColor.GRAY + "");
        zLoc.setPosition(60, 80, Anchor.LEFT | Anchor.TOP);

        UILabel facingLabel = new UILabel(this, ChatColor.WHITE + "Facing:");
        facingLabel.setPosition(5, 90, Anchor.LEFT | Anchor.TOP);
        directionLoc = new UILabel(this, ChatColor.GRAY + "direction");
        directionLoc.setPosition(60, 90, Anchor.LEFT | Anchor.TOP);

        UILabel biomeLabel = new UILabel(this, ChatColor.WHITE + "Biome:");
        biomeLabel.setPosition(5, 100, Anchor.LEFT | Anchor.TOP);
        biomeName = new UILabel(this, ChatColor.GRAY + "biomeName");
        biomeName.setPosition(60, 100, Anchor.LEFT | Anchor.TOP);

        UILabel lightLabel = new UILabel(this, ChatColor.WHITE + "Lighting:");
        lightLabel.setPosition(5, 110, Anchor.LEFT | Anchor.TOP);
        blockLight = new UILabel(this, ChatColor.GRAY + "blockLight");
        blockLight.setPosition(60, 120, Anchor.LEFT | Anchor.TOP);
        skyLight = new UILabel(this, ChatColor.GRAY + "skyLight");
        skyLight.setPosition(60, 130, Anchor.LEFT | Anchor.TOP);
        rawLight = new UILabel(this, ChatColor.GRAY + "rawLight");
        rawLight.setPosition(60, 140, Anchor.LEFT | Anchor.TOP);

        UILabel version = new UILabel(this, ChatColor.GREEN + Almura.GUI_VERSION);
        version.setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM);
        version.setFontScale(0.7f);

        debugPanel.add(debugTitle, fpsLabel, fps, memoryLabel, memoryDebug, memoryAllocated, directionLabel, xLoc, yLoc, zLoc, facingLabel,
                       directionLoc,
                       biomeLabel, biomeName, lightLabel, blockLight, skyLight, rawLight, version);

        addToScreen(debugPanel);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (UPDATES_ENABLED && mc.thePlayer != null && mc.currentScreen == null && Configuration.DISPLAY_ENHANCED_DEBUG) {
            updateWidgets();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.DEBUG && Configuration.DISPLAY_ENHANCED_DEBUG) {
            if (UPDATES_ENABLED && mc.thePlayer != null) {
                event.setCanceled(true);

                if (mc.currentScreen == null) {
                    setWorldAndResolution(mc, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
                    drawScreen(event.mouseX, event.mouseY, event.partialTicks);
                }
            }
        }
    }

    public void updateWidgets() {
        long maxMemory = RUNTIME.maxMemory();
        long totalMemory = RUNTIME.totalMemory();
        long freeMemory = RUNTIME.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        boolean dirtyCoords = false;

        if (x == Integer.MIN_VALUE || mc.thePlayer.posX != mc.thePlayer.prevPosX) {
            x = MathHelper.floor_double(mc.thePlayer.posX);
            xLoc.setText(ChatColor.GRAY + String
                    .format("- x: %.5f (%d) // chunk: %d (%d)", mc.thePlayer.posX, x, mc.thePlayer.chunkCoordX, x & 15));
            dirtyCoords = true;
        }

        if (y == Integer.MIN_VALUE || mc.thePlayer.posY != mc.thePlayer.prevPosY) {
            y = MathHelper.floor_double(mc.thePlayer.posY);
            yLoc.setText(ChatColor.GRAY + String.format("- y: %.3f (%d) ", mc.thePlayer.posY, y));
            dirtyCoords = true;
        }

        if (z == Integer.MIN_VALUE || mc.thePlayer.posZ != mc.thePlayer.prevPosZ) {
            z = MathHelper.floor_double(mc.thePlayer.posZ);
            zLoc.setText(ChatColor.GRAY + String
                    .format("- z: %.5f (%d) // chunk: %d (%d)", mc.thePlayer.posZ, z, mc.thePlayer.chunkCoordZ, z & 15));
            dirtyCoords = true;
        }

        if (yaw == Integer.MIN_VALUE || mc.thePlayer.rotationYawHead != mc.thePlayer.prevRotationYawHead) {
            yaw = MathHelper.floor_double((double) (mc.thePlayer.rotationYawHead * 4.0F / 360.0F) + 0.5D) & 3;
            final String direction = Direction.directions[yaw];
            directionLoc.setText(ChatColor.GRAY + direction.substring(0, 1).toUpperCase() + direction.substring(1).toLowerCase());
        }

        if (dirtyCoords) {
            if (chunk == null || x != (int) mc.thePlayer.prevPosX || z != mc.thePlayer.prevPosZ) {
                chunk = mc.thePlayer.worldObj.getChunkFromChunkCoords(mc.thePlayer.chunkCoordX, mc.thePlayer.chunkCoordZ);
            }

            biomeName.setText(ChatColor.GRAY + chunk.getBiomeGenForWorldCoords(x & 15, z & 15, mc.theWorld.getWorldChunkManager()).biomeName);
            blockLight.setText(ChatColor.GRAY + "- block: " + chunk.getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15));
            skyLight.setText(ChatColor.GRAY + "- sky: " + chunk.getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15));
            rawLight.setText(ChatColor.GRAY + "- raw: " + chunk.getBlockLightValue(x & 15, y, z & 15, 0));
        }

        final int displayFps = mc.debugFPS;
        fps.setText(ChatColor.GOLD + "" + displayFps);
        memoryDebug.setText(
                ChatColor.GRAY + "Used: " + usedMemory * 100L / maxMemory + "% (" + usedMemory / 1024L / 1024L + "MB) of " + maxMemory / 1024L / 1024L
                + "MB");
        memoryAllocated.setText(ChatColor.GRAY + "Allocated memory: " + totalMemory * 100L / maxMemory + "% (" + totalMemory / 1024L / 1024L + "MB)");
    }
}
