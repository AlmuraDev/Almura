/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.world.biome;

import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeColorHelper.class)
public interface BiomeColorHelperAccessor {
    //public net.minecraft.world.biome.BiomeColorHelper field_180290_c # WATER_COLOR
    @Accessor("WATER_COLOR") static BiomeColorHelper.ColorResolver accessor$getWaterColor() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
