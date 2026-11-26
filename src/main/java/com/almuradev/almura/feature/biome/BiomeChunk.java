/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The server's biomes for one chunk on the client.
 *
 * <p>Read from the chunk render worker threads as well as the main thread. The config of each column is
 * looked up once here; the temperature noise of each column is worked out on first use and kept (any
 * thread may fill it in, and they all get the same value).</p>
 */
@SideOnly(Side.CLIENT)
public final class BiomeChunk {

    private final int[] biomeArray;
    private final BiomeConfig[] columnConfigs = new BiomeConfig[256];
    private final float[] columnNoise = new float[256];

    BiomeChunk(final BiomeClientFeature registry, final int[] biomeArray) {
        this.biomeArray = biomeArray;

        final Map<Integer, BiomeConfig> configs = new HashMap<>();
        for (int index = 0; index < 256; index++) {
            final int biomeId = this.biomeArray[index];
            if (!configs.containsKey(biomeId)) {
                configs.put(biomeId, registry.getBiomeConfig(biomeId));
            }
            this.columnConfigs[index] = configs.get(biomeId);
        }
        Arrays.fill(this.columnNoise, Float.NaN);
    }

    public String getBiomeRegistryName(final BlockPos pos, Biome fallback) {
        final BiomeConfig config = this.getConfig(pos);
        if (config == null) {
            return fallback.getRegistryName().toString();
        }

        return config.getRegistryKey();
    }

    public String getBiomeName(final BlockPos pos, Biome fallback) {
        final BiomeConfig config = this.getConfig(pos);
        if (config == null) {
            return fallback.getBiomeName();
        }

        return config.getBiomeName();
    }

    public float getTemperature(final BlockPos pos, Biome fallback) {
        final BiomeConfig config = this.getConfig(pos);
        if (config == null) {
            return fallback.getTemperature(pos);
        }

        return this.getScaledTemperature(config, pos);
    }

    public float getRainfall(final BlockPos pos, Biome fallback) {
        final BiomeConfig config = this.getConfig(pos);
        if (config == null) {
            return fallback.getRainfall();
        }

        return config.getRainfall();
    }

    public int getWaterColor(final BlockPos pos, Biome fallback) {
        final BiomeConfig config = this.getConfig(pos);
        if (config == null) {
            return fallback.getWaterColor();
        }

        return config.getWaterColor();
    }

    /**
     * @return The server's config for the biome at this position, or null if the server didn't send one
     */
    @Nullable
    public BiomeConfig getConfig(final BlockPos pos) {
        return this.columnConfigs[index(pos)];
    }

    /**
     * Same as {@link BiomeUtil#getScaledTemperature(BiomeConfig, BlockPos)}, with the column's noise kept.
     */
    public float getScaledTemperature(final BiomeConfig config, final BlockPos pos) {
        if (pos.getY() <= 64) {
            return config.getDefaultTemperature();
        }
        final int index = index(pos);
        float noise = this.columnNoise[index];
        if (Float.isNaN(noise)) {
            noise = BiomeUtil.getTemperatureNoise(pos.getX(), pos.getZ());
            this.columnNoise[index] = noise;
        }
        return BiomeUtil.getScaledTemperature(config, pos.getY(), noise);
    }

    private static int index(final BlockPos pos) {
        return (pos.getZ() & 15) << 4 | (pos.getX() & 15);
    }
}
