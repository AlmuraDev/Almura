/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

import com.almuradev.content.registry.ContentBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;
import java.util.stream.Collectors;

public final class BlockDecayActionBuilder extends ContentBuilder.Impl<BlockDecayAction> implements BlockDecayAction.Builder {
    private final Int2ObjectMap<BlockDecayAction.Entry.Builder> entries = new Int2ObjectOpenHashMap<>();

    @Override
    public BlockDecayAction.Entry.Builder entry(final int index) {
        BlockDecayAction.Entry.Builder builder = this.entries.get(index);
        if (builder != null) {
            return builder;
        }
        builder = new BlockDecayActionEntry.Builder();
        this.entries.put(index, builder);
        return builder;
    }

    @Override
    public BlockDecayAction build() {
        final List<BlockDecayAction.Entry> entries = this.entries.values().stream()
                .map(BlockDecayAction.Entry.Builder::build)
                .collect(Collectors.toList());
        return new BlockDecayActionImpl(this.id, this.name, entries);
    }
}
