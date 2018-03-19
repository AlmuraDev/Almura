/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.core.event.Witness;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
@SideOnly(Side.CLIENT)
public final class BiomeClientFeature implements Witness {

    private final Long2ObjectMap<BiomeChunk> biomeChunkCache = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>(8192));
    private Map<Integer, BiomeConfig> biomeConfigs = new HashMap<>();

    public void setBiomeConfigs(final Map<Integer, BiomeConfig> biomeConfigs) {
        this.biomeConfigs = biomeConfigs;
    }

    @Nullable
    public BiomeConfig getBiomeConfig(final int biomeId) {
        return this.biomeConfigs.get(biomeId);
    }

    public void createChunk(long chunkKey, final int[] biomeArray) {
        this.biomeChunkCache.put(chunkKey, new BiomeChunk(this, biomeArray));
    }

    public void removeChunk(long chunkKey) {
        this.biomeChunkCache.remove(chunkKey);
    }

    @Nullable
    public BiomeChunk getChunk(BlockPos pos) {
        final int cx = pos.getX() >> 4;
        final int cz = pos.getZ() >> 4;

        return this.biomeChunkCache.get(ChunkPos.asLong(cx, cz));
    }

    @SubscribeEvent
    public void onClientConnectionToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.biomeConfigs.clear();
        this.biomeChunkCache.clear();
    }
}
