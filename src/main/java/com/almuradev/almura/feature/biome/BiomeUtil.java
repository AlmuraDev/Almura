/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.asm.mixin.accessors.world.biome.BiomeAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class BiomeUtil {

    @Inject
    private static BiomeClientFeature registry;

    @Nullable
    public static BiomeChunk getChunk(BlockPos pos) {
        return registry.getChunk(pos);
    }

    public static float getScaledTemperature(BiomeConfig biomeConfig, BlockPos pos) {
        if (pos.getY() > 64) {
            return getScaledTemperature(biomeConfig, pos.getY(), getTemperatureNoise(pos.getX(), pos.getZ()));
        } else {
            return biomeConfig.getDefaultTemperature();
        }
    }

    /**
     * @return The temperature noise of a column, which only depends on its x and z
     */
    static float getTemperatureNoise(final int x, final int z) {
        return (float) (BiomeAccessor.accessor$getTemperatureNoise().getValue((double) ((float) x / 8.0F), (double) ((float) z / 8.0F)) * 4.0D);
    }

    /**
     * @return The temperature above y 64, given the column's {@link #getTemperatureNoise(int, int) noise}
     */
    static float getScaledTemperature(final BiomeConfig biomeConfig, final int y, final float noise) {
        return biomeConfig.getDefaultTemperature() - (noise + (float) y - 64.0F) * 0.05F / 30.0F;
    }
}
