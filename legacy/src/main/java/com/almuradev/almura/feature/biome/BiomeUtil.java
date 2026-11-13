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
            float f = (float) (BiomeAccessor.accessor$getTemperatureNoise().getValue((double) ((float) pos.getX() / 8.0F), (double) ((float) pos.getZ() / 8.0F)) * 4.0D);
            return biomeConfig.getDefaultTemperature() - (f + (float) pos.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return biomeConfig.getDefaultTemperature();
        }
    }
}
