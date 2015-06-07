/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.util.Colors;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.text.WordUtils;

public class IngameDebugHUD extends SimpleGui {

    public static final Runtime RUNTIME = Runtime.getRuntime();

    private UILabel fps, memoryDebug, memoryAllocated, xLoc, yLoc, zLoc, directionLoc, biomeName, blockLight, skyLight, rawLight;
    private Chunk chunk;

    public IngameDebugHUD() {
        guiscreenBackground = false;
    }

    @Override
    public void construct() {
        FontRenderOptions labelsFro = new FontRenderOptions();
        labelsFro.apply(EnumChatFormatting.WHITE);

        FontRenderOptions valuesFro = new FontRenderOptions();
        valuesFro.apply(EnumChatFormatting.GRAY);

        int x = 60;

        // Construct Hud with all elements
        final UIBackgroundContainer debugPanel = new UIBackgroundContainer(this);
        debugPanel.setSize(285, 175);
        debugPanel.setPosition(5, 0, Anchor.MIDDLE);
        debugPanel.setColor(0);
        debugPanel.setBackgroundAlpha(180);
        debugPanel.setClipContent(false);

        // Title
        UILabel debugTitle = new UILabel(this, Colors.AQUA + "Almura Debug");
        debugTitle.setPosition(0, 3, Anchor.CENTER);
        debugTitle.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_110);

        // FPS
        UILabel fpsLabel = new UILabel(this, "FPS :");
        fpsLabel.setPosition(5, 20).setFontRenderOptions(labelsFro);

        fps = new UILabel(this);
        fps.setPosition(32, 20).setFontRenderOptions(valuesFro);
        FontRenderOptions fro = new FontRenderOptions();
        fro.apply(EnumChatFormatting.GOLD);
        fps.setFontRenderOptions(fro);

        // MC Memory Usage
        UILabel memoryLabel = new UILabel(this, "Memory :");
        memoryLabel.setPosition(5, 30).setFontRenderOptions(labelsFro);

        memoryDebug = new UILabel(this);
        memoryDebug.setPosition(x, 30).setFontRenderOptions(valuesFro);
        memoryAllocated = new UILabel(this);
        memoryAllocated.setPosition(x, 40).setFontRenderOptions(valuesFro);

        // Location Stats
        UILabel directionLabel = new UILabel(this, "Directions :");
        directionLabel.setPosition(5, 50).setFontRenderOptions(labelsFro);

        xLoc = new UILabel(this);
        xLoc.setPosition(x, 60).setFontRenderOptions(valuesFro);
        yLoc = new UILabel(this);
        yLoc.setPosition(x, 70).setFontRenderOptions(valuesFro);
        zLoc = new UILabel(this);
        zLoc.setPosition(x, 80).setFontRenderOptions(valuesFro);

        UILabel facingLabel = new UILabel(this, "Facing :");
        facingLabel.setPosition(5, 90).setFontRenderOptions(labelsFro);

        directionLoc = new UILabel(this);
        directionLoc.setPosition(x, 90).setFontRenderOptions(valuesFro);

        UILabel biomeLabel = new UILabel(this, "Biome :");
        biomeLabel.setPosition(5, 100).setFontRenderOptions(labelsFro);

        biomeName = new UILabel(this);
        biomeName.setPosition(x, 100).setFontRenderOptions(valuesFro);

        UILabel lightLabel = new UILabel(this, "Lighting :");
        lightLabel.setPosition(5, 110).setFontRenderOptions(labelsFro);

        blockLight = new UILabel(this);
        blockLight.setPosition(x, 120).setFontRenderOptions(valuesFro);
        skyLight = new UILabel(this);
        skyLight.setPosition(x, 130).setFontRenderOptions(valuesFro);
        rawLight = new UILabel(this);
        rawLight.setPosition(x, 140).setFontRenderOptions(valuesFro);

        UILabel version = new UILabel(this, Colors.GREEN + Almura.GUI_VERSION);
        version.setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM);
        version.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_070);

        debugPanel.add(debugTitle, fpsLabel, fps, memoryLabel, memoryDebug, memoryAllocated, directionLabel, xLoc, yLoc, zLoc, facingLabel,
                directionLoc,
                biomeLabel, biomeName, lightLabel, blockLight, skyLight, rawLight, version);

        addToScreen(debugPanel);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {

        float mb = 1F / 1024 / 1024;
        int maxMemory = (int) (RUNTIME.maxMemory() * mb);
        int totalMemory = (int) (RUNTIME.totalMemory() * mb);
        int freeMemory = (int) (RUNTIME.freeMemory() * mb);
        long usedMemory = totalMemory - freeMemory;
        EntityClientPlayerMP p = mc.thePlayer;

        fps.setText("" + Minecraft.debugFPS);

        memoryDebug.setText("Used : " + usedMemory * 100L / maxMemory + "% (" + usedMemory + "MB) of " + maxMemory + "MB");
        memoryAllocated.setText(Colors.GRAY + "Allocated memory: " + totalMemory * 100L / maxMemory + "% (" + totalMemory + "MB)");

        xLoc.setText(String.format("- x : %.5f (%d) // chunk: %d (%d)", p.posX, (int) p.posX, p.chunkCoordX, (int) p.posX & 15));

        yLoc.setText(String.format("- y : %.3f (%d) ", p.posY, (int) p.posY));

        zLoc.setText(String.format("- z : %.5f (%d) // chunk: %d (%d)", p.posZ, (int) p.posZ, p.chunkCoordZ, (int) p.posZ & 15));

        int yaw = MathHelper.floor_double(p.rotationYawHead * 4.0F / 360.0F + 0.5D) & 3;
        directionLoc.setText(WordUtils.capitalizeFully(Direction.directions[yaw]));

        if (chunk == null || p.posX != p.prevPosX || p.posZ != p.prevPosZ)
        {
            chunk = p.worldObj.getChunkFromChunkCoords(p.chunkCoordX, p.chunkCoordZ);
            int x = (int) p.posX & 15;
            int y = (int) p.posY;
            int z = (int) p.posZ & 15;

            biomeName.setText(chunk.getBiomeGenForWorldCoords(x, z, mc.theWorld.getWorldChunkManager()).biomeName);
            blockLight.setText("- block : " + chunk.getSavedLightValue(EnumSkyBlock.Block, x, y, z));
            skyLight.setText("- sky : " + chunk.getSavedLightValue(EnumSkyBlock.Sky, x, y, z));
            rawLight.setText("- raw : " + chunk.getBlockLightValue(x, y, z, 0));
        }

    }
}
