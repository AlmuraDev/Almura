/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy;

import com.almuradev.content.registry.ContentBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;
import java.util.stream.Collectors;

public final class BlockDestroyActionBuilder extends ContentBuilder.Impl<BlockDestroyAction> implements BlockDestroyAction.Builder {
    private final Int2ObjectMap<BlockDestroyAction.Entry.Builder> entries = new Int2ObjectOpenHashMap<>();

    @Override
    public BlockDestroyAction.Entry.Builder entry(final int index) {
        BlockDestroyAction.Entry.Builder builder = this.entries.get(index);
        if (builder != null) {
            return builder;
        }
        builder = new BlockDestroyActionEntry.Builder();
        this.entries.put(index, builder);
        return builder;
    }

    @Override
    public BlockDestroyAction build() {
        final List<BlockDestroyAction.Entry> entries = this.entries.values().stream()
                .map(BlockDestroyAction.Entry.Builder::build)
                .collect(Collectors.toList());
        return new BlockDestroyActionImpl(this.id, this.name, entries);
    }
}
