/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.context;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public final class EverythingApplyContext implements BlockApplyContext, ItemApplyContext {
    private final Random random;
    private final BlockPos pos;
    private final IBlockState state;
    private final ItemStack item;

    public EverythingApplyContext(final Random random, final BlockPos pos, final IBlockState state, final ItemStack item) {
        this.random = random;
        this.pos = pos;
        this.state = state;
        this.item = item;
    }

    @Override
    public Random random() {
        return this.random;
    }

    @Override
    public BlockPos pos() {
        return this.pos;
    }

    @Override
    public IBlockState state() {
        return this.state;
    }

    @Override
    public ItemStack item() {
        return this.item;
    }
}
