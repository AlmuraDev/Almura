/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class IngameDebugHUD extends MalisisGui {
    public UILabel fps, memoryDebug, memoryAllocated, xLoc, yLoc, zLoc, directionLoc, biomeName, blockLight, skyLight, rawLight;
    public boolean enableUpdates = false;
    
    public IngameDebugHUD() {
       
        guiscreenBackground = false; // prevent full screen black background.
        
        // Construct Hud with all elements
        final UIBackgroundContainer debugPanel = new UIBackgroundContainer(this);
        debugPanel.setSize(300, 175);
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
        fpsLabel.setPosition(5,20, Anchor.LEFT | Anchor.TOP);        
        fps = new UILabel(this, "fpsDrawsHere");
        fps.setPosition(32,20, Anchor.LEFT | Anchor.TOP);
        
        // MC Memory Usage
        UILabel memoryLabel = new UILabel(this, ChatColor.WHITE + "Memory: ");
        memoryLabel.setPosition(5,30, Anchor.LEFT | Anchor.TOP);        
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
        directionLoc.setPosition(60,90, Anchor.LEFT | Anchor.TOP);
        
        UILabel biomeLabel = new UILabel(this, ChatColor.WHITE + "Biome:");
        biomeLabel.setPosition(5,100, Anchor.LEFT | Anchor.TOP);        
        biomeName = new UILabel(this, ChatColor.GRAY + "biomeName");
        biomeName.setPosition(60,100, Anchor.LEFT | Anchor.TOP);
        
        UILabel lightLabel = new UILabel(this, ChatColor.WHITE + "Lighting:");
        lightLabel.setPosition(5,110, Anchor.LEFT | Anchor.TOP);
        blockLight = new UILabel(this, ChatColor.GRAY + "blockLight");
        blockLight.setPosition(60, 120, Anchor.LEFT | Anchor.TOP);
        skyLight = new UILabel(this, ChatColor.GRAY + "skyLight");
        skyLight.setPosition(60, 130, Anchor.LEFT | Anchor.TOP);
        rawLight = new UILabel(this, ChatColor.GRAY + "rawLight");
        rawLight.setPosition(60, 140, Anchor.LEFT | Anchor.TOP);
        
        UILabel version = new UILabel(this, ChatColor.GREEN + Almura.VERSION_STRING);
        version.setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM);
       
                
        debugPanel.add(debugTitle);
        debugPanel.add(fpsLabel);
        debugPanel.add(fps);       
        debugPanel.add(memoryLabel);
        debugPanel.add(memoryDebug);
        debugPanel.add(memoryAllocated);
        debugPanel.add(directionLabel);
        debugPanel.add(xLoc);        
        debugPanel.add(yLoc);        
        debugPanel.add(zLoc);
        debugPanel.add(facingLabel);
        debugPanel.add(directionLoc);
        debugPanel.add(biomeLabel);
        debugPanel.add(biomeName);
        debugPanel.add(lightLabel);
        debugPanel.add(blockLight);
        debugPanel.add(skyLight);
        debugPanel.add(rawLight);
        debugPanel.add(version);
        
        addToScreen(debugPanel);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(WorldTickEvent event) {
        if (enableUpdates) {            
            updateWidgets();
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.DEBUG) {
            event.setCanceled(true);
            enableUpdates = true;
            setWorldAndResolution(Minecraft.getMinecraft(), event.resolution.getScaledWidth(), event.resolution.getScaledHeight());            
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
            
        }
    }
    
    public void updateWidgets() {
        long var41 = Runtime.getRuntime().maxMemory();
        long var34 = Runtime.getRuntime().totalMemory();
        long var42 = Runtime.getRuntime().freeMemory();
        long var43 = var34 - var42;
        
        int var47 = MathHelper.floor_double(this.mc.thePlayer.posX);
        int var22 = MathHelper.floor_double(this.mc.thePlayer.posY);
        int var23 = MathHelper.floor_double(this.mc.thePlayer.posZ);
        int var24 = MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        Chunk var48 = this.mc.theWorld.getChunkFromBlockCoords(var47, var23);

        String displayFps = mc.debug.split("fps")[0];
        String direction = Direction.directions[var24];
        String cleanDirection = direction.substring(0,1).toUpperCase()+direction.substring(1).toLowerCase();
        
        fps.setText(ChatColor.GOLD + displayFps);        
        memoryDebug.setText(ChatColor.GRAY + "Used: " + var43 * 100L / var41 + "% (" + var43 / 1024L / 1024L + "MB) of " + var41 / 1024L / 1024L + "MB");
        memoryAllocated.setText(ChatColor.GRAY + "Allocated memory: " + var34 * 100L / var41 + "% (" + var34 / 1024L / 1024L + "MB)");
        xLoc.setText(ChatColor.GRAY + String.format("- x: %.5f (%d) // chunk: %d (%d)", new Object[] {Double.valueOf(this.mc.thePlayer.posX), Integer.valueOf(var47), Integer.valueOf(var47 >> 4), Integer.valueOf(var47 & 15)}));
        yLoc.setText(ChatColor.GRAY + String.format("- y: %.3f ", new Object[] {Double.valueOf(this.mc.thePlayer.boundingBox.minY), Double.valueOf(this.mc.thePlayer.posY)}));
        zLoc.setText(ChatColor.GRAY + String.format("- z: %.5f (%d) // chunk: %d (%d)", new Object[] {Double.valueOf(this.mc.thePlayer.posZ), Integer.valueOf(var23), Integer.valueOf(var23 >> 4), Integer.valueOf(var23 & 15)}));
        directionLoc.setText(ChatColor.GRAY + cleanDirection);
        biomeName.setText(ChatColor.GRAY + var48.getBiomeGenForWorldCoords(var47 & 15, var23 & 15, this.mc.theWorld.getWorldChunkManager()).biomeName);
        blockLight.setText(ChatColor.GRAY + "- block: " + var48.getSavedLightValue(EnumSkyBlock.Block, var47 & 15, var22, var23 & 15));
        skyLight.setText(ChatColor.GRAY + "- sky: " + var48.getSavedLightValue(EnumSkyBlock.Sky, var47 & 15, var22, var23 & 15));
        rawLight.setText(ChatColor.GRAY + "- raw: " + var48.getBlockLightValue(var47 & 15, var22, var23 & 15, 0));
    }
}
