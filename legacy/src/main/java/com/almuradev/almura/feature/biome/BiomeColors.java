/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Colors of TerrainControl biomes on the client.
 *
 * <p>TerrainControl biomes reach the client saved as the vanilla biome they replace (Plains, Forest, ...).
 * The server sends the real biome of every column and its config (see {@link BiomeChunk}), and the biome
 * color mixins ask this class for the color. A null result means "not a TerrainControl biome here, use
 * vanilla's color".</p>
 *
 * <p>The color rules match TerrainControl's old client code: a color of white ({@link BiomeConfig#NO_COLOR})
 * means not set, a multiplier color is averaged with the climate color, and any other color is used as is.</p>
 */
@SideOnly(Side.CLIENT)
public final class BiomeColors {

    private BiomeColors() {
    }

    @Nullable
    public static BiomeConfig getTerrainControlConfig(final BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return null;
        }
        final BiomeConfig config = chunk.getConfig(pos);
        if (config == null || !config.hasTerrainControlColors()) {
            return null;
        }
        return config;
    }

    @Nullable
    public static Integer getGrassColor(final Biome biome, final BlockPos pos) {
        final BiomeConfig config = getTerrainControlConfig(pos);
        if (config == null) {
            return null;
        }
        final double temperature = MathHelper.clamp(BiomeUtil.getScaledTemperature(config, pos), 0.0F, 1.0F);
        final double rainfall = MathHelper.clamp(config.getRainfall(), 0.0F, 1.0F);
        final int climateColor = biome.getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(temperature, rainfall));
        return applyColor(climateColor, config.getGrassColor(), config.isGrassColorMultiplier());
    }

    @Nullable
    public static Integer getFoliageColor(final Biome biome, final BlockPos pos) {
        final BiomeConfig config = getTerrainControlConfig(pos);
        if (config == null) {
            return null;
        }
        final double temperature = MathHelper.clamp(BiomeUtil.getScaledTemperature(config, pos), 0.0F, 1.0F);
        final double rainfall = MathHelper.clamp(config.getRainfall(), 0.0F, 1.0F);
        final int climateColor = biome.getModdedBiomeFoliageColor(ColorizerFoliage.getFoliageColor(temperature, rainfall));
        return applyColor(climateColor, config.getFoliageColor(), config.isFoliageColorMultiplier());
    }

    @Nullable
    public static Integer getWaterColor(final BlockPos pos) {
        final BiomeConfig config = getTerrainControlConfig(pos);
        return config == null ? null : config.getWaterColor();
    }

    @Nullable
    public static Integer getSkyColor(final BlockPos pos) {
        final BiomeConfig config = getTerrainControlConfig(pos);
        return config == null ? null : config.getSkyColor();
    }

    private static int applyColor(final int climateColor, final int configColor, final boolean isMultiplier) {
        if (configColor == BiomeConfig.NO_COLOR) {
            // Not set. Many configs say "not a multiplier" next to the white default, which would
            // otherwise give white grass.
            return climateColor;
        }
        if (isMultiplier) {
            // Average of the two, per color channel
            return ((climateColor & 0xFEFEFE) + (configColor & 0xFEFEFE)) >> 1;
        }
        return configColor;
    }
}
