/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.LazyDelegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockType;

abstract class AbstractLazyBlockState implements LazyBlockState {
    protected final Delegate<BlockType> block;
    protected final Delegate<IBlockState> state;

    AbstractLazyBlockState(final Delegate<BlockType> block) {
        this.block = block;
        this.state = new LazyDelegate<IBlockState>() {
            @Override
            protected IBlockState load() {
                return AbstractLazyBlockState.this.createState();
            }
        };
    }

    @SuppressWarnings("unused")
    abstract <T extends Comparable<T>> IBlockState createState();

    @Override
    public final Block block() {
        return (Block) this.block.require();
    }

    @Override
    public final IBlockState get() {
        return this.state.get();
    }

    @Override
    public final boolean fullTest(final IBlockState state) {
        return this.get().equals(state);
    }

    @Override
    @SuppressWarnings("unused")
    public final <V extends Comparable<V>> boolean partialTest(final IBlockState state) {
        if (!this.block.require().equals(state.getBlock())) {
            return false;
        }
        return this.partialTest0(state);
    }

    @SuppressWarnings("unused")
    protected abstract <V extends Comparable<V>> boolean partialTest0(final IBlockState state);
}
