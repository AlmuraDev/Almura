/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.io.Serializable;

public final class BiomeConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private int serverBiomeId;
    private String registryKey;
    private float temperature = 0f;
    private float rainfall = 0f;
    private int waterColor;

    public BiomeConfig() {
    }

    public BiomeConfig(final int serverBiomeId, final String registryKey, final float temperature, final float rainfall, final int waterColor) {
        this.serverBiomeId = serverBiomeId;
        this.registryKey = registryKey;
        this.temperature = temperature;
        this.rainfall = rainfall;
        this.waterColor = waterColor;
    }

    public static BiomeConfig of(Biome biome) {
        return new BiomeConfig(((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(biome), biome.getRegistryName().toString(), biome
                .getDefaultTemperature(), biome.getRainfall(), biome.waterColor);
    }

    public int getServerBiomeId() {
        return this.serverBiomeId;
    }

    public String getRegistryKey() {
        return this.registryKey;
    }

    public float getDefaultTemperature() {
        return this.temperature;
    }

    public float getRainfall() {
        return this.rainfall;
    }

    public int getWaterColor() {
        return this.waterColor;
    }
}
