/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.value.StateValue;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockType;

import java.util.Optional;

final class StatelessLazyBlockState extends AbstractLazyBlockState {
    StatelessLazyBlockState(final Delegate<BlockType> block) {
        super(block);
    }

    @Override
    IBlockState createState() {
        return ((Block) this.block.require()).getDefaultState();
    }

    @Override
    public <V extends Comparable<V>> Optional<StateValue<V>> value(final IProperty<V> property) {
        return Optional.empty();
    }

    @Override
    protected boolean partialTest0(final IBlockState state) {
        return true;
    }
}
