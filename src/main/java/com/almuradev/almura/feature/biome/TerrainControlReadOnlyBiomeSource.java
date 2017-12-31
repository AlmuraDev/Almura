/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.google.common.base.MoreObjects;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.inject.Inject;

public final class TerrainControlReadOnlyBiomeSource extends ReadOnlyBiomeSource {
    private final HeadUpDisplay hud;

    @Inject
    private TerrainControlReadOnlyBiomeSource(final HeadUpDisplay hud) {
        this.hud = hud;
    }

    @Override
    public ReadOnlyBiome getBiome(final World world, final BlockPos pos) {
        @Nullable final LocalWorld lw = TerrainControl.getWorld(MoreObjects.firstNonNull(this.hud.worldName, WorldHelper.getName(world)));
        if (lw != null) {
            final LocalBiome biome = lw.getBiome(pos.getX(), pos.getZ());
            return new ReadOnlyBiome() {
                @Override
                public String name() {
                    return biome.getName();
                }

                @Override
                public float temperature(BlockPos pos) {
                    return biome.getTemperatureAt(pos.getX(), pos.getY(), pos.getZ());
                }
            };
        }
        return super.getBiome(world, pos);
    }
}
