/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStairs.class)
public interface BlockStairsAccessor {
    @Mutable @Accessor("modelBlock") void accessor$setModelBlock(Block block);
    @Mutable @Accessor("modelState") void accessor$setModelState(IBlockState state);
}
