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
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class BiomeChunk {

    private final int[] biomeArray;
    private final Map<Integer, BiomeConfig> configs;

    BiomeChunk(final BiomeClientFeature registry, final int[] biomeArray) {
        this.biomeArray = biomeArray;
        this.configs = new HashMap<>(256);

        for (final int biomeId : this.biomeArray) {
            if (!this.configs.containsKey(biomeId)) {
                this.configs.put(biomeId, registry.getBiomeConfig(biomeId));
            }
        }
    }

    public String getBiomeRegistryName(final BlockPos pos, Biome fallback) {
        final int biomeId = this.getBiomeId(pos);

        final BiomeConfig config = this.configs.get(biomeId);
        if (config == null) {
            return fallback.getRegistryName().toString();
        }

        return config.getRegistryKey();
    }

    public float getTemperature(final BlockPos pos, Biome fallback) {
        final int biomeId = this.getBiomeId(pos);

        final BiomeConfig config = this.configs.get(biomeId);
        if (config == null) {
            return fallback.getTemperature(pos);
        }

        return BiomeUtil.getScaledTemperature(config, pos);
    }

    public float getRainfall(final BlockPos pos, Biome fallback) {
        final int biomeId = this.getBiomeId(pos);

        final BiomeConfig config = this.configs.get(biomeId);
        if (config == null) {
            return fallback.getRainfall();
        }

        return config.getRainfall();
    }

    public int getWaterColor(final BlockPos pos, Biome fallback) {
        final int biomeId = this.getBiomeId(pos);

        final BiomeConfig config = this.configs.get(biomeId);
        if (config == null) {
            return fallback.getWaterColor();
        }

        return config.getWaterColor();
    }

    private int getBiomeId(final BlockPos pos) {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        return this.biomeArray[j << 4 | i];
    }
}
