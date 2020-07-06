/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel.debug;

import com.almuradev.almura.Almura;
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
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class InformationDebugPanel extends AbstractDebugPanel {
    private static final double XYZ_SINGLE_LINE_MAX = 100000d;
    private static final String DEFAULT_VERSION = "dev";
    private final Text title;
    private final int titleWidth;

    public InformationDebugPanel(final MalisisGui gui, final int width, final int height, final Game game) {
        super(gui, width, height);
        this.title = Text.of(
                Text.of(TextStyles.UNDERLINE, TextColors.GREEN, 'm' + game.getPlatform().getMinecraftVersion().getName()),
                TextStyles.NONE, " ",
                Text.of(TextStyles.UNDERLINE, TextColors.RED, 'f' + StringUtils.substringAfterLast(pluginVersion(game, "forge"), ".")),
                TextStyles.NONE, " ",
                Text.of(TextStyles.UNDERLINE, TextColors.GOLD, 's' + StringUtils.substringAfterLast(platformContainerVersion(game, Platform.Component.IMPLEMENTATION), "-")),
                TextStyles.NONE, " ",
                Text.of(TextStyles.UNDERLINE, TextColors.DARK_AQUA, 'b' + StringUtils.substringAfterLast(pluginVersion(game, Almura.ID), "-b"))
        );
        this.titleWidth = this.client.fontRenderer.getStringWidth(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.title)) / 2;
    }

    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        @Nullable final Entity view = this.cameraView();
        if (view == null) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.client.profiler.startSection("debug");

        this.renderTitle();

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
            this.drawProperty("Chunk-relative", String.format("%d %d %d", fx & 0xf, fy & 0xf, fz & 0xf), 4);
        } else {
            final BlockPos pos = new BlockPos(x, y, z);
            final Chunk chunk = view.getEntityWorld().getChunk(pos);

            this.renderXYZ(x, y, z);
            this.renderFacing(view.getHorizontalFacing(), view.rotationYaw, view.rotationPitch);
            this.drawProperty("Block", fx + ", " + fy + ", " + fz, 4);
            this.drawProperty("Chunk", String.format("%d, %d, %d in %d, %d, %d", fx & 0xf, fy & 0xf, fz & 0xf, fx >> 4, fy >> 4, fz >> 4), 4);
            if (view.getEntityWorld().isBlockLoaded(pos) && pos.getY() >= 0 && pos.getY() < 256 && !chunk.isEmpty()) {
                final Biome biome = chunk.getBiome(pos, this.client.world.getBiomeProvider());
                this.drawProperty("Biome Reg", this.getBiomeRegistryName(biome, pos), 4);
                this.drawProperty("Biome Name", this.getBiomeName(biome, pos), 4);
                this.drawProperty("Biome ID", String.valueOf(this.getBiomeId(biome, pos)), 4);
                this.drawProperty("Temperature", "" + this.getTemperature(biome, pos),4);
                this.drawProperty("Rainfall", "" + this.getRainfall(biome, pos), 4);
                this.drawProperty("Light", getLightDetails(pos, chunk), 4);
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

        this.autoSize();

        this.client.profiler.endSection();
    }

    private String getBiomeRegistryName(Biome biome, BlockPos pos) {
        return biome.getRegistryName().toString();
    }

    private int getBiomeId(Biome biome, BlockPos pos) {
        return Biome.getIdForBiome(biome);
    }

    private String getBiomeName(Biome biome, BlockPos pos) {
        return biome.getBiomeName();
    }

    private float getTemperature(Biome biome, BlockPos pos) {
        return biome.getTemperature(pos);
    }

    private float getRainfall(Biome biome, BlockPos pos) {
        return biome.getRainfall();
    }

    private void renderTitle() {
        this.drawText(this.title, (Math.max(this.autoWidth, this.baseWidth) / 2) - this.titleWidth, this.autoHeight);
        if (this.autoSize) {
            this.autoWidth = 0;
        }
    }

    private void renderGame() {
        this.drawProperty("Java", this.getJavaDetails(), 4, this.autoHeight += 4);
        this.drawProperty("Memory", getMemoryDetails(), 4);
        this.drawProperty("FPS", String.valueOf(Minecraft.getDebugFPS()), 4);
    }

    private void renderXYZ(final double x, final double y, final double z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty("X", String.format("%.3f", x), 4);
            this.drawProperty("Y", String.format("%.3f", y), 4);
            this.drawProperty("Z", String.format("%.3f", z), 4);
        } else {
            this.drawProperty("XYZ", String.format("%.3f, %.3f, %.3f", x, y, z), 4);
        }
    }

    private void renderFacing(final EnumFacing facing, final float yaw, final float pitch) {
        final String facingTowards = describeFacing(facing);
        this.drawProperty("Facing", String.format("%s%s (%.1f, %.1f)", facing.name().charAt(0), facingTowards,
                MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)), 4);
    }

    private void renderLook(final int x, final int y, final int z) {
        if (x >= XYZ_SINGLE_LINE_MAX || y >= XYZ_SINGLE_LINE_MAX || z >= XYZ_SINGLE_LINE_MAX) {
            this.drawProperty("Look X", String.format("%d", x), 4);
            this.drawProperty("Look Y", String.format("%d", y), 4);
            this.drawProperty("Look Z", String.format("%d", z), 4);
        } else {
            this.drawProperty("Look", String.format("%d, %d, %d", x, y, z), 4);
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

    private static String pluginVersion(final Game game, final String id) {
        return game.getPluginManager().getPlugin(id).flatMap(PluginContainer::getVersion).orElse(DEFAULT_VERSION);
    }

    private static String platformContainerVersion(final Game game, final Platform.Component component) {
        return game.getPlatform().getContainer(component).getVersion().orElse(DEFAULT_VERSION);
    }
}
