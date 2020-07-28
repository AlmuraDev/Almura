/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.util;

import com.almuradev.almura.asm.mixin.accessors.server.management.PlayerChunkMapAccessor;
import com.google.common.collect.AbstractIterator;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;

// TODO This is literally insanity, do something better in the future....
public final class CustomChunkIterator extends AbstractIterator<Chunk> {

    private final WorldServer world;
    private final Iterator<PlayerChunkMapEntry> iterator;

    public CustomChunkIterator(final WorldServer world, final Iterator<PlayerChunkMapEntry> iterator) {
        this.world = world;
        this.iterator = iterator;
    }

    @Override
    protected Chunk computeNext() {
        while (true) {
            if (iterator.hasNext()) {
                PlayerChunkMapEntry playerchunkmapentry = iterator.next();
                Chunk chunk = playerchunkmapentry.getChunk();

                if (chunk == null) {
                    continue;
                }

                if (!chunk.isLightPopulated() && chunk.isTerrainPopulated()) {
                    return chunk;
                }

                if (!chunk.wasTicked()) {
                    return chunk;
                }

                if (world.getWorldInfo().getWorldName().equalsIgnoreCase("orilla")) {
                    return chunk;
                }

                //Todo: make this a configurable / toggleable option (in-realtime)
                if (!playerchunkmapentry.hasPlayerMatchingInRange(128.0D, PlayerChunkMapAccessor.accessor$getNotSpectator())) {
                    continue;
                }

                return chunk;
            }

            return this.endOfData();
        }
    }
}
