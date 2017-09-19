/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud.debug;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class UIDebugDetailsPanel extends AbstractDebugPanel {

    private static final double XYZ_SINGLE_LINE_MAX = 100000d;
    private final Text title;
    private final int titleWidth;

    public UIDebugDetailsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        final String title = "Minecraft " + this.client.getVersion();
        this.title = Text.of(TextColors.GOLD, title);
        this.titleWidth = this.client.fontRenderer.getStringWidth(title) / 2;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        final Entity view = this.getView();
        if (view == null) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.client.mcProfiler.startSection("debug");

        this.drawText(renderer, this.title, (Math.max(this.autoWidth, this.baseWidth) / 2) - this.titleWidth - 2, 4);
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
        final BlockPos pos = new BlockPos(x, y, z);
        final Chunk chunk = view.getEntityWorld().getChunkFromBlockCoords(pos);

        // Standard details
        this.drawProperty(renderer, "Java", this.getJavaDetails(), 4, this.autoHeight += 4);
        this.drawProperty(renderer, "Memory", this.getMemoryDetails(), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "FPS", String.valueOf(Minecraft.getDebugFPS()), 4, this.getAutoSizeHeight());
        // Respect reduced debug
        final boolean reducedDebug = this.client.isReducedDebug();
        if (reducedDebug) {
            this.drawProperty(renderer, "Chunk-relative", String.format("%d %d %d", fx & 15, fy & 15, fz & 15), 4, this.getAutoSizeHeight());
        } else {
            this.renderXYZ(renderer, x, y, z);
            final EnumFacing facing = view.getHorizontalFacing();
            final String facingTowards = describeFacing(facing);
            this.drawProperty(renderer, "Facing", String.format("%s%s (%.1f, %.1f)", facing.name().charAt(0), facingTowards,
                    MathHelper.wrapDegrees(view.rotationYaw), MathHelper.wrapDegrees(view.rotationPitch)), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Block", fx + ", " + fy + ", " + fz, 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Chunk", String.format("%d %d %d in %d %d %d", fx & 15, fy & 15, fz & 15, fx >> 4, fy >> 4, fz >> 4), 4, this.getAutoSizeHeight());
            if (view.getEntityWorld().isBlockLoaded(pos) && pos.getY() >= 0 && pos.getY() < 256 && !chunk.isEmpty()) {
                this.drawProperty(renderer, "Biome", chunk.getBiome(pos, this.client.world.getBiomeProvider()).getBiomeName(),
                        4, this.getAutoSizeHeight());
                this.drawProperty(renderer, "Light", this.getLightDetails(pos, chunk), 4, this.getAutoSizeHeight());
            }
        }

        // Draw block we're currently looking at
        final RayTraceResult omo = this.client.objectMouseOver;
        if (omo != null && omo.typeOfHit == RayTraceResult.Type.BLOCK && omo.getBlockPos() != null) {
            final BlockPos lookPos = omo.getBlockPos();

            // Respect reduced debug
            if (!reducedDebug) {
                this.renderLook(renderer, lookPos.getX(), lookPos.getY(), lookPos.getZ());
            }
        }

        // AutoSizing
        if (this.autoSize) {
            this.setSize(Math.max(this.getAutoSizeWidth(), this.baseWidth), Math.max(this.getAutoSizeHeight(), this.baseHeight));
            this.autoHeight = 0;
        }

        this.client.mcProfiler.endSection();
    }

    private void renderXYZ(final GuiRenderer renderer, final double x, final double y, final double z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty(renderer, "X", String.format("%.3f", x), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Y", String.format("%.3f", y), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Z", String.format("%.3f", z), 4, this.getAutoSizeHeight());
        } else {
            this.drawProperty(renderer, "XYZ", String.format("%.3f, %.3f, %.3f", x, y, z), 4, this.getAutoSizeHeight());
        }
    }

    private void renderLook(final GuiRenderer renderer, final int x, final int y, final int z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty(renderer, "Look X", String.format("%d", x), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Look Y", String.format("%d", y), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Look Z", String.format("%d", z), 4, this.getAutoSizeHeight());
        } else {
            this.drawProperty(renderer, "Look", String.format("%d, %d, %d", x, y, z), 4, this.getAutoSizeHeight());
        }
    }

    private String getJavaDetails() {
        return String.format("%s x%d", System.getProperty("java.version"), (this.client.isJava64bit() ? 64 : 86));
    }

    private String getMemoryDetails() {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long usedMemory = totalMemory - freeMemory;
        return (usedMemory * 100L / maxMemory) + "%% " + convertBytesToMegabytes(usedMemory) + "/" + convertBytesToMegabytes(maxMemory) + "MB";
    }

    private String getLightDetails(BlockPos viewerPos, Chunk chunk) {
        return String.valueOf(chunk.getLightSubtracted(viewerPos, 0)) +
                " (" + chunk.getLightFor(EnumSkyBlock.SKY, viewerPos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, viewerPos) + " block)";
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

    private static long convertBytesToMegabytes(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
