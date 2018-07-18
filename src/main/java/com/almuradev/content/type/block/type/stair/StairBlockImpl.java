/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinition;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public final class StairBlockImpl extends BlockStairs implements StairBlock {
    private final StairBlockStateDefinition definition;
    private final LazyBlockState block;

    StairBlockImpl(final StairBlockBuilder builder) {
        // TODO This too early, fix better
        super(builder.block.get());
        this.displayOnCreativeTab = null;
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.block = builder.block;
    }

    @Override
    public StairBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }
}
