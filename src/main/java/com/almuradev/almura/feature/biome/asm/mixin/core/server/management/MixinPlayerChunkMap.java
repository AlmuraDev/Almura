/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.server.management;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap {

    @Shadow @Final private WorldServer world;
    @Shadow private final List<PlayerChunkMapEntry> entries = Lists.<PlayerChunkMapEntry>newArrayList();

    /**
     * @author Dockter
     * Purpose: experimental test to bypass OR remove player range check for certain worlds.
     */
    @Overwrite
    public Iterator<Chunk> getChunkIterator() {
        final Iterator<PlayerChunkMapEntry> iterator = this.entries.iterator();
        return new AbstractIterator<Chunk>() {
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
                        if (!playerchunkmapentry.hasPlayerMatchingInRange(128.0D, PlayerChunkMap.NOT_SPECTATOR)) {
                            continue;
                        }

                        return chunk;
                    }

                    return (Chunk)this.endOfData();
                }
            }
        };
    }
}
