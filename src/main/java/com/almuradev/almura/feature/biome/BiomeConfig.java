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
    // TerrainControl colors, only set for TerrainControl biomes (see TerrainControlBiomeColors)
    private boolean hasTerrainControlColors;
    private int grassColor = NO_COLOR;
    private boolean grassColorIsMultiplier;
    private int foliageColor = NO_COLOR;
    private boolean foliageColorIsMultiplier;
    private int skyColor;

    /**
     * TerrainControl's value for "no grass/foliage color set": white, as a multiplier, changes nothing.
     */
    public static final int NO_COLOR = 0xFFFFFF;

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
        final BiomeConfig config = new BiomeConfig(((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(biome), biome.getRegistryName().toString(), ((BiomeAccessor)biome).accessor$getBiomeName(), biome.getDefaultTemperature(), biome.getRainfall(), ((BiomeAccessor)biome).accessor$getWaterColor());

        final TerrainControlBiomeColors colors = TerrainControlBiomeColors.of(biome);
        if (colors != null) {
            config.hasTerrainControlColors = true;
            config.grassColor = colors.grassColor;
            config.grassColorIsMultiplier = colors.grassColorIsMultiplier;
            config.foliageColor = colors.foliageColor;
            config.foliageColorIsMultiplier = colors.foliageColorIsMultiplier;
            config.skyColor = colors.skyColor;
        }
        return config;
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

    /**
     * @return Whether this is a TerrainControl biome, whose colors the client should use instead of the
     * colors of the vanilla biome it's saved as
     */
    public boolean hasTerrainControlColors() {
        return this.hasTerrainControlColors;
    }

    public int getGrassColor() {
        return this.grassColor;
    }

    public boolean isGrassColorMultiplier() {
        return this.grassColorIsMultiplier;
    }

    public int getFoliageColor() {
        return this.foliageColor;
    }

    public boolean isFoliageColorMultiplier() {
        return this.foliageColorIsMultiplier;
    }

    public int getSkyColor() {
        return this.skyColor;
    }
}
