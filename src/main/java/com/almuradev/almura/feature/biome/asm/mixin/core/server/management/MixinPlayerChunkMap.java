/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.server.management;

import com.almuradev.almura.feature.biome.util.CustomChunkIterator;
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
     * @reason Experimental test to bypass OR remove player range check for certain worlds.
     */
    @Overwrite
    public Iterator<Chunk> getChunkIterator() {
        final Iterator<PlayerChunkMapEntry> iterator = this.entries.iterator();
        return new CustomChunkIterator(this.world, iterator);
    }
}
