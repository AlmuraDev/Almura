/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.context;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface BlockApplyContext extends ApplyContext {
    BlockPos pos();

    IBlockState state();

    default Block block() {
        return this.state().getBlock();
    }
}
