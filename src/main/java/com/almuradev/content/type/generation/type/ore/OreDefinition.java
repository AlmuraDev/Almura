/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.world.WorldProvider;
import org.spongepowered.api.block.BlockType;

import javax.annotation.Nullable;

public interface OreDefinition {
    Delegate<BlockType> block();

    String dimension();

    int size();

    int count();

    boolean accepts(final WorldProvider dimension);

    interface Builder {
        void block(final Delegate<BlockType> block);

        void size(final int size);

        void count(final int count);

        OreDefinition build(final OreGenerator.Builder builder);

        final class Impl implements Builder {
            @Nullable Delegate<BlockType> block;
            int size;
            int count;

            @Override
            public void block(final Delegate<BlockType> block) {
                this.block = block;
            }

            @Override
            public void size(final int size) {
                this.size = size;
            }

            @Override
            public void count(final int count) {
                this.count = count;
            }

            @Override
            public OreDefinition build(final OreGenerator.Builder builder) {
                return new OreDefinitionImpl(this, builder);
            }
        }
    }
}
