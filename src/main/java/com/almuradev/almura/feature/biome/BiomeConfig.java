/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.asm.mixin.accessors.world.biome.BiomeAccessor;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.io.Serializable;

public final class BiomeConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private int serverBiomeId;
    private String registryKey;
    private String biomeName;
    private float temperature = 0f;
    private float rainfall = 0f;
    private int waterColor;

    public BiomeConfig() {
    }

    public BiomeConfig(final int serverBiomeId, final String registryKey, final String biomeName, final float temperature, final float rainfall, final int waterColor) {
        this.serverBiomeId = serverBiomeId;
        this.registryKey = registryKey;
        this.biomeName = biomeName;
        this.temperature = temperature;
        this.rainfall = rainfall;
        this.waterColor = waterColor;
    }

    public static BiomeConfig of(Biome biome) {
        return new BiomeConfig(((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(biome), biome.getRegistryName().toString(), ((BiomeAccessor)biome).accessor$getBiomeName(), biome.getDefaultTemperature(), biome.getRainfall(), ((BiomeAccessor)biome).accessor$getWaterColor());
    }

    public int getServerBiomeId() {
        return this.serverBiomeId;
    }

    public String getBiomeName() {
        return this.biomeName;
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
