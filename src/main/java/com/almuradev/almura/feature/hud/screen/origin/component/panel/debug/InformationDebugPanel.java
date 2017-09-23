/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel.debug;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class InformationDebugPanel extends AbstractDebugPanel {

    private static final String GAME_NAME = "Minecraft";
    private static final double XYZ_SINGLE_LINE_MAX = 100000d;
    private final Text title;
    private final int titleWidth;

    public InformationDebugPanel(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height);

        final String title = GAME_NAME + ' ' + this.client.getVersion();
        this.title = Text.of(TextColors.GOLD, title);
        this.titleWidth = this.client.fontRenderer.getStringWidth(title) / 2;
    }

    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        final Entity view = this.getView();
        if (view == null) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.client.mcProfiler.startSection("debug");

        this.drawText(this.title, (Math.max(this.autoWidth, this.baseWidth) / 2) - this.titleWidth - 2, 4);
        // Reset autoWidth after we've drawn the title
        if (this.autoSize) {
            this.autoWidth = 0;
        }

        // Position
        final double x = view.posX;
        final int fx = MathHelper.floor(x);
        final double y = view.getEntityBoundingBox().minY; // vanilla uses minY of the AABB
        final int fy = MathHelper.floor(y);
        final double z = view.posZ;
        final int fz = MathHelper.floor(z);

        // Game information should always render...
        this.renderGame();

        // ...but the rest depends on the reduced debug value.
        final boolean reducedDebug = this.client.isReducedDebug();
        if (reducedDebug) {
            this.drawProperty("Chunk-relative", String.format("%d %d %d", fx & 0xf, fy & 0xf, fz & 0xf), 4, this.autoHeight);
        } else {
            final BlockPos pos = new BlockPos(x, y, z);
            final Chunk chunk = view.getEntityWorld().getChunkFromBlockCoords(pos);

            this.renderXYZ(x, y, z);
            this.renderFacing(view.getHorizontalFacing(), view.rotationYaw, view.rotationPitch);
            this.drawProperty("Block", fx + ", " + fy + ", " + fz, 4, this.autoHeight);
            this.drawProperty("Chunk", String.format("%d, %d, %d in %d, %d, %d", fx & 0xf, fy & 0xf, fz & 0xf, fx >> 4, fy >> 4, fz >> 4), 4, this.autoHeight);
            if (view.getEntityWorld().isBlockLoaded(pos) && pos.getY() >= 0 && pos.getY() < 256 && !chunk.isEmpty()) {
                final Biome biome = chunk.getBiome(pos, this.client.world.getBiomeProvider());
                this.drawProperty("Biome", biome.getBiomeName(), 4, this.autoHeight);
                this.drawProperty("Light", getLightDetails(pos, chunk), 4, this.autoHeight);
            }
        }

        // Draw block we're currently looking at
        final RayTraceResult omo = this.client.objectMouseOver;
        if (omo != null && omo.typeOfHit == RayTraceResult.Type.BLOCK && omo.getBlockPos() != null) {
            final BlockPos lookPos = omo.getBlockPos();

            // Respect reduced debug
            if (!reducedDebug) {
                this.renderLook(lookPos.getX(), lookPos.getY(), lookPos.getZ());
            }
        }

        if (this.autoSize) {
            this.setSize(Math.max(this.autoSizeWidth(), this.baseWidth), Math.max(this.autoHeight, this.baseHeight));
            this.autoHeight = 0;
        }

        this.client.mcProfiler.endSection();
    }

    private void renderGame() {
        this.drawProperty("Java", this.getJavaDetails(), 4, this.autoHeight += 4);
        this.drawProperty("Memory", getMemoryDetails(), 4, this.autoHeight);
        this.drawProperty("FPS", String.valueOf(Minecraft.getDebugFPS()), 4, this.autoHeight);
    }

    private void renderXYZ(final double x, final double y, final double z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty("X", String.format("%.3f", x), 4, this.autoHeight);
            this.drawProperty("Y", String.format("%.3f", y), 4, this.autoHeight);
            this.drawProperty("Z", String.format("%.3f", z), 4, this.autoHeight);
        } else {
            this.drawProperty("XYZ", String.format("%.3f, %.3f, %.3f", x, y, z), 4, this.autoHeight);
        }
    }

    private void renderFacing(final EnumFacing facing, final float yaw, final float pitch) {
        final String facingTowards = describeFacing(facing);
        this.drawProperty("Facing", String.format("%s%s (%.1f, %.1f)", facing.name().charAt(0), facingTowards,
                MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)), 4, this.autoHeight);
    }

    private void renderLook(final int x, final int y, final int z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty("Look X", String.format("%d", x), 4, this.autoHeight);
            this.drawProperty("Look Y", String.format("%d", y), 4, this.autoHeight);
            this.drawProperty("Look Z", String.format("%d", z), 4, this.autoHeight);
        } else {
            this.drawProperty("Look", String.format("%d, %d, %d", x, y, z), 4, this.autoHeight);
        }
    }

    private String getJavaDetails() {
        return String.format("%s x%d", System.getProperty("java.version"), this.client.isJava64bit() ? 64 : 86);
    }

    private static String getLightDetails(final BlockPos pos, final Chunk chunk) {
        final int sky = chunk.getLightFor(EnumSkyBlock.SKY, pos);
        final int block = chunk.getLightFor(EnumSkyBlock.BLOCK, pos);
        return chunk.getLightSubtracted(pos, 0) + " (" + sky + " sky, " + block + " block)";
    }

    private static String describeFacing(final EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return " (-z)";
            case SOUTH:
                return " (+z)";
            case WEST:
                return " (-x)";
            case EAST:
                return " (+x)";
            default: // invalid
                return "";
        }
    }

    private static String getMemoryDetails() {
        final long max = Runtime.getRuntime().maxMemory();
        final long total = Runtime.getRuntime().totalMemory();
        final long free = Runtime.getRuntime().freeMemory();
        final long used = total - free;
        return (used * 100L / max) + "%% " + convertBytesToMegabytes(used) + "/" + convertBytesToMegabytes(max) + "MB";
    }

    private static long convertBytesToMegabytes(final long bytes) {
        return bytes / 1024L / 1024L;
    }
}
