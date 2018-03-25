/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world;

import com.almuradev.almura.feature.biome.asm.mixin.iface.IMixinChunk;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(Chunk.class)
public abstract class MixinChunk implements IMixinChunk {

    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private World world;

    private int[] extendedBiomeArray;

    @Override
    public int[] cacheRealBiomeIds() {
        this.extendedBiomeArray = new int[256];
        Arrays.fill(this.extendedBiomeArray, Biome.getIdForBiome(Biomes.DEFAULT));

        final Biome[] biomes = this.world.provider.getBiomeProvider().getBiomes(null, this.x * 16, this.z * 16, 16, 16);
        for (int i = 0; i < biomes.length; i++) {
            this.extendedBiomeArray[i] = Biome.getIdForBiome(biomes[i]);
        }

        return this.extendedBiomeArray;
    }

    @Override
    public int[] getExtendedBiomeArray() {
        return this.extendedBiomeArray;
    }
}