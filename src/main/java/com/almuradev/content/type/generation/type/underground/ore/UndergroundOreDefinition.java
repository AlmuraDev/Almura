/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore;

import com.almuradev.content.type.block.state.LazyBlockState;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nullable;

public interface UndergroundOreDefinition {
    LazyBlockState block();

    int size();

    int count();

    boolean accepts(final World world);

    interface Builder {
        void block(final LazyBlockState block);

        void size(final int size);

        void count(final int count);

        void worlds(final List<String> worlds);

        UndergroundOreDefinition build(final UndergroundOreGenerator.Builder builder);

        final class Impl implements Builder {
            @Nullable LazyBlockState block;
            int size;
            int count;
            List<String> worlds;

            @Override
            public void block(final LazyBlockState block) {
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
            public void worlds(final List<String> worlds) {
                this.worlds = worlds;
            }

            @Override
            public UndergroundOreDefinition build(final UndergroundOreGenerator.Builder builder) {
                return new UndergroundOreDefinitionImpl(this, builder);
            }
        }
    }
}
