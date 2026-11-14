/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A biome's temperature at a position, adjusted for height (26.3 keeps it private): what Almura's crops grow by, as 1.12's
 * {@code Biome.getTemperature(pos)} was.
 */
@Mixin(Biome.class)
public interface BiomeAccessor {

    @Invoker("getTemperature")
    float almura$getTemperature(BlockPos pos, int seaLevel);
}
