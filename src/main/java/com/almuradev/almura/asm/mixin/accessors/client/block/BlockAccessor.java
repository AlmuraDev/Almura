/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessor {
    //public net.minecraft.block.Block field_149772_a # displayOnCreativeTab
    @Accessor("displayOnCreativeTab") CreativeTabs accessor$getDisplayOnCreativeTab();
    @Accessor("displayOnCreativeTab") void accessor$setDisplayOnCreativeTab(CreativeTabs tabs);

    //public-f net.minecraft.block.Block field_176227_L # blockState
    @Mutable @Accessor("blockState") BlockStateContainer accessor$getBlockState();
    @Mutable @Accessor("blockState") void accessor$setBlockState(BlockStateContainer state);
}
