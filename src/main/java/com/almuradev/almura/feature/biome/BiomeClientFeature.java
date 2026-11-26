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

    /*
     * The last chunk looked up. Rebuilding a chunk's graphics asks for the same chunk thousands of times, from
     * the main thread and the render worker threads, so this saves going through the locked map each time.
     * Adding or removing a chunk bumps the generation, which makes any remembered chunk stale.
     */
    private volatile int generation;
    @Nullable private volatile LastChunk lastChunk;

    public void setBiomeConfigs(final Map<Integer, BiomeConfig> biomeConfigs) {
        this.biomeConfigs = biomeConfigs;
    }

    @Nullable
    public BiomeConfig getBiomeConfig(final int biomeId) {
        return this.biomeConfigs.get(biomeId);
    }

    public void createChunk(long chunkKey, final int[] biomeArray) {
        this.biomeChunkCache.put(chunkKey, new BiomeChunk(this, biomeArray));
        this.invalidateLastChunk();
    }

    public void removeChunk(long chunkKey) {
        this.biomeChunkCache.remove(chunkKey);
        this.invalidateLastChunk();
    }

    @Nullable
    public BiomeChunk getChunk(BlockPos pos) {
        final int cx = pos.getX() >> 4;
        final int cz = pos.getZ() >> 4;
        final long chunkKey = ChunkPos.asLong(cx, cz);

        // Read the generation before the map, so a chunk found just before a change is never remembered as current
        final int generation = this.generation;
        final LastChunk last = this.lastChunk;
        if (last != null && last.chunkKey == chunkKey && last.generation == generation) {
            return last.chunk;
        }

        final BiomeChunk chunk = this.biomeChunkCache.get(chunkKey);
        if (chunk != null) {
            this.lastChunk = new LastChunk(chunkKey, generation, chunk);
        }
        return chunk;
    }

    @SubscribeEvent
    public void onClientConnectionToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.biomeConfigs.clear();
        this.biomeChunkCache.clear();
        this.invalidateLastChunk();
    }

    private synchronized void invalidateLastChunk() {
        this.generation++;
        this.lastChunk = null;
    }

    private static final class LastChunk {

        final long chunkKey;
        final int generation;
        final BiomeChunk chunk;

        LastChunk(final long chunkKey, final int generation, final BiomeChunk chunk) {
            this.chunkKey = chunkKey;
            this.generation = generation;
            this.chunk = chunk;
        }
    }
}
