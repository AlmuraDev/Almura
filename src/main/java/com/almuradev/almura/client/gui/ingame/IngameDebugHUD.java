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
import com.almuradev.almurasdk.util.FileSizeUtil;
import com.flowpowered.math.vector.Vector3i;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.text.WordUtils;

public class IngameDebugHUD extends SimpleGui {

    public static final Runtime RUNTIME = Runtime.getRuntime();
    private static final FontRenderOptions KEY_FRO = new FontRenderOptions();
    private static final FontRenderOptions VALUE_FRO = new FontRenderOptions();
    private static final String MEMORY_FORMAT = "Used: %s\n"
            + "Free: %s\n"
            + "Allocated: %s";
    private static final String LOCATION_FORMAT = "X: %.3f // chunk: %d (%d)\n"
            + "Y: %.3f\n"
            + "Z: %.3f // chunk: %d (%d)";
    private static final String LIGHTING_FORMAT = "Block: %d\n"
            + "Sky: %d\n"
            + "Raw: %d";

    private final UIBackgroundContainer container = new UIBackgroundContainer(this);
    private UILabel titleLabel, fpsKeyLabel, fpsValueLabel, memoryKeyLabel, memoryValueLabel, locationKeyLabel, locationValueLabel, facingKeyLabel,
            facingValueLabel, biomeKeyLabel, biomeValueLabel, lightingKeyLabel, lightingValueLabel;
    private Chunk chunk;
    private final int padding = 5;
    private final int innerXPadding = 3;
    private final int innerYPadding = 2;

    static {
        KEY_FRO.apply(EnumChatFormatting.WHITE);
        VALUE_FRO.apply(EnumChatFormatting.GRAY);
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        // Container
        container.setSize(180, 175);
        container.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        container.setColor(0);
        container.setBackgroundAlpha(180);
        container.setClipContent(false);

        // Title
        titleLabel = new UILabel(this, String.format(Colors.AQUA + "Debug (%s)", Colors.GREEN + Almura.GUI_VERSION + Colors.AQUA));
        titleLabel.setPosition(0, 3, Anchor.TOP | Anchor.CENTER);
        titleLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_110);

        // FPS
        fpsKeyLabel = new UILabel(this, Colors.WHITE + "FPS:");
        fpsKeyLabel.setPosition(padding, getPaddedY(titleLabel, innerYPadding));
        fpsKeyLabel.setFontRenderOptions(KEY_FRO);

        fpsValueLabel = new UILabel(this);
        fpsValueLabel.setPosition(getPaddedX(fpsKeyLabel, innerXPadding), fpsKeyLabel.getY());
        fpsValueLabel.setFontRenderOptions(VALUE_FRO);

        // Memory
        memoryKeyLabel = new UILabel(this, Colors.WHITE + "Memory:");
        memoryKeyLabel.setPosition(padding, getPaddedY(fpsValueLabel, innerYPadding));
        memoryKeyLabel.setFontRenderOptions(KEY_FRO);

        memoryValueLabel = new UILabel(this, true);
        memoryValueLabel.setPosition(padding * 2, getPaddedY(memoryKeyLabel, innerYPadding));
        memoryValueLabel.setFontRenderOptions(VALUE_FRO);
        memoryValueLabel.setText(MEMORY_FORMAT);
        memoryValueLabel.setSize(memoryValueLabel.getWidth(), 30);

        // Location
        locationKeyLabel = new UILabel(this, Colors.WHITE + "Location:");
        locationKeyLabel.setPosition(padding, getPaddedY(memoryValueLabel, innerYPadding));
        locationKeyLabel.setFontRenderOptions(KEY_FRO);

        locationValueLabel = new UILabel(this, true);
        locationValueLabel.setPosition(padding * 2, getPaddedY(locationKeyLabel, innerYPadding));
        locationValueLabel.setFontRenderOptions(VALUE_FRO);
        locationValueLabel.setText(LOCATION_FORMAT);
        locationValueLabel.setSize(locationValueLabel.getWidth(), 30);

        // Facing
        facingKeyLabel = new UILabel(this, Colors.WHITE + "Facing:");
        facingKeyLabel.setPosition(padding, getPaddedY(locationValueLabel, innerYPadding));
        facingKeyLabel.setFontRenderOptions(KEY_FRO);

        facingValueLabel = new UILabel(this);
        facingValueLabel.setPosition(getPaddedX(facingKeyLabel, innerXPadding), facingKeyLabel.getY());
        facingValueLabel.setFontRenderOptions(VALUE_FRO);

        // Biome
        biomeKeyLabel = new UILabel(this, Colors.WHITE + "Biome:");
        biomeKeyLabel.setPosition(padding, getPaddedY(facingValueLabel, innerYPadding));
        biomeKeyLabel.setFontRenderOptions(KEY_FRO);

        biomeValueLabel = new UILabel(this);
        biomeValueLabel.setPosition(getPaddedX(biomeKeyLabel, innerXPadding), biomeKeyLabel.getY());
        biomeValueLabel.setFontRenderOptions(VALUE_FRO);

        // Lighting
        lightingKeyLabel = new UILabel(this, Colors.WHITE + "Lighting:");
        lightingKeyLabel.setPosition(padding, getPaddedY(biomeValueLabel, innerYPadding));
        lightingKeyLabel.setFontRenderOptions(KEY_FRO);

        lightingValueLabel = new UILabel(this, true);
        lightingValueLabel.setPosition(padding * 2, getPaddedY(lightingKeyLabel, innerYPadding));
        lightingValueLabel.setFontRenderOptions(VALUE_FRO);
        lightingValueLabel.setText(LIGHTING_FORMAT);

        container.add(titleLabel, fpsKeyLabel, fpsValueLabel, memoryKeyLabel, memoryValueLabel, locationKeyLabel, locationValueLabel,
                facingKeyLabel, facingValueLabel, biomeKeyLabel, biomeValueLabel, lightingKeyLabel, lightingValueLabel);

        addToScreen(container);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {

        fpsValueLabel.setText(Colors.GRAY + String.valueOf(Minecraft.debugFPS));

        memoryValueLabel.setText(Colors.GRAY + String.format(MEMORY_FORMAT,
                FileSizeUtil.format(RUNTIME.totalMemory() - RUNTIME.freeMemory()),
                FileSizeUtil.format(RUNTIME.freeMemory()),
                RUNTIME.totalMemory() * 100L / RUNTIME.maxMemory() + "% (" + FileSizeUtil.format(RUNTIME.totalMemory()) + ")"));

        locationValueLabel.setText(Colors.GRAY + String.format(LOCATION_FORMAT,
                mc.thePlayer.posX, mc.thePlayer.chunkCoordX, (int) mc.thePlayer.posX & 15,
                mc.thePlayer.posY,
                mc.thePlayer.posZ, mc.thePlayer.chunkCoordZ, (int) mc.thePlayer.posZ & 15));

        final int yaw = MathHelper.floor_double(mc.thePlayer.rotationYawHead * 4F / 360F + 0.5D) & 3;
        facingValueLabel.setText(Colors.GRAY + WordUtils.capitalizeFully(Direction.directions[yaw]));

        if (chunk == null || mc.thePlayer.posX != mc.thePlayer.prevPosX || mc.thePlayer.posZ != mc.thePlayer.prevPosZ) {
            chunk = mc.thePlayer.worldObj.getChunkFromChunkCoords(mc.thePlayer.chunkCoordX, mc.thePlayer.chunkCoordZ);

            final Vector3i location = new Vector3i((int) mc.thePlayer.posX & 15, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ & 15);
            biomeValueLabel.setText(Colors.GRAY + chunk.getBiomeGenForWorldCoords(location.getX(), location.getZ(), mc.theWorld.getWorldChunkManager()).biomeName);
            lightingValueLabel.setText(Colors.GRAY + String.format(LIGHTING_FORMAT,
                    chunk.getSavedLightValue(EnumSkyBlock.Block, location.getX(), location.getY(), location.getZ()),
                    chunk.getSavedLightValue(EnumSkyBlock.Sky, location.getX(), location.getY(), location.getZ()),
                    chunk.getBlockLightValue(location.getX(), location.getY(), location.getZ(), 0)));
        }
    }
}
