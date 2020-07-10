/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeAccessor {
    //public net.minecraft.world.biome.Biome field_76791_y # biomeName
    @Accessor("biomeName") String accessor$getBiomeName();
    //public net.minecraft.world.biome.Biome field_76759_H # waterColor
    @Accessor("waterColor") int accessor$getWaterColor();
    //public net.minecraft.world.biome.Biome field_150605_ac # TEMPERATURE_NOISE
    @Accessor("TEMPERATURE_NOISE") static NoiseGeneratorPerlin accessor$getTemperatureNoise() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
