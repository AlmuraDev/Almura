/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class ReadOnlyBiomeSource {
    public ReadOnlyBiome getBiome(final World world, final BlockPos pos) {
        final Biome biome = world.getBiome(pos);
        return new ReadOnlyBiome() {
            @Override
            public String name() {
                return biome.biomeName;
            }

            @Override
            public float temperature(final BlockPos pos) {
                return biome.getTemperature(pos);
            }
        };
    }
}
