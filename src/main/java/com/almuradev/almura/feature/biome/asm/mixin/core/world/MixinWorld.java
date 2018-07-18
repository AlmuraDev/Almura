/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world;

import com.almuradev.almura.feature.biome.BiomeChunk;
import com.almuradev.almura.feature.biome.BiomeUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class MixinWorld {

    @Redirect(method = "isRainingAt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;canSnowAt(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean redirectCanSnowAt(World world, BlockPos pos, boolean checkLight) {
        final BiomeChunk biomeChunk = BiomeUtil.getChunk(pos);

        if (biomeChunk == null) {
            return world.canSnowAt(pos, checkLight);
        }

        final Biome biome = world.getBiome(pos);
        final float temperature = biomeChunk.getTemperature(pos, biome);

        return temperature < 0.15f;
    }
}
