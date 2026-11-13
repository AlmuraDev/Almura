/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import net.minecraft.world.biome.Biome;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

/**
 * Reads the colors of a TerrainControl biome on the server, so they can be sent to clients.
 *
 * <p>Uses reflection so Almura doesn't need TerrainControl to compile or run. TerrainControl's
 * biomes ({@code TXBiome}) keep their config in a public {@code biomeConfig} field, which holds
 * the colors as public fields.</p>
 */
final class TerrainControlBiomeColors {

    private static final String TC_BIOME_CLASS = "com.khorn.terraincontrol.forge.generator.TXBiome";

    private static boolean lookedUpFields;
    @Nullable private static Field biomeConfigField;
    @Nullable private static Field grassColorField;
    @Nullable private static Field grassColorIsMultiplierField;
    @Nullable private static Field foliageColorField;
    @Nullable private static Field foliageColorIsMultiplierField;
    @Nullable private static Field skyColorField;

    final int grassColor;
    final boolean grassColorIsMultiplier;
    final int foliageColor;
    final boolean foliageColorIsMultiplier;
    final int skyColor;

    private TerrainControlBiomeColors(final int grassColor, final boolean grassColorIsMultiplier, final int foliageColor,
            final boolean foliageColorIsMultiplier, final int skyColor) {
        this.grassColor = grassColor;
        this.grassColorIsMultiplier = grassColorIsMultiplier;
        this.foliageColor = foliageColor;
        this.foliageColorIsMultiplier = foliageColorIsMultiplier;
        this.skyColor = skyColor;
    }

    /**
     * @return The colors, or null if the biome isn't a TerrainControl biome
     */
    @Nullable
    static TerrainControlBiomeColors of(final Biome biome) {
        if (!biome.getClass().getName().equals(TC_BIOME_CLASS)) {
            return null;
        }

        try {
            if (!lookedUpFields) {
                lookedUpFields = true;
                biomeConfigField = biome.getClass().getField("biomeConfig");
                final Class<?> configClass = biomeConfigField.getType();
                grassColorField = configClass.getField("grassColor");
                grassColorIsMultiplierField = configClass.getField("grassColorIsMultiplier");
                foliageColorField = configClass.getField("foliageColor");
                foliageColorIsMultiplierField = configClass.getField("foliageColorIsMultiplier");
                skyColorField = configClass.getField("skyColor");
            }
            if (biomeConfigField == null) {
                return null;
            }

            final Object config = biomeConfigField.get(biome);
            if (config == null) {
                return null;
            }
            return new TerrainControlBiomeColors(grassColorField.getInt(config), grassColorIsMultiplierField.getBoolean(config),
                    foliageColorField.getInt(config), foliageColorIsMultiplierField.getBoolean(config), skyColorField.getInt(config));
        } catch (ReflectiveOperationException | RuntimeException e) {
            // A TerrainControl version with other field names: fall back to vanilla colors
            biomeConfigField = null;
            return null;
        }
    }
}
