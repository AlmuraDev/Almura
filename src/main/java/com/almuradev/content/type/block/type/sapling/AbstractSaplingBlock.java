/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public abstract class AbstractSaplingBlock extends BlockSapling {
    public AbstractSaplingBlock() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(STAGE, 0));
    }

    @Override
    public abstract void generateTree(final World world, final BlockPos pos, final IBlockState state, final Random random);

    @Override
    public boolean isTypeAt(final World world, final BlockPos pos, final BlockPlanks.EnumType type) {
        return world.getBlockState(pos).getBlock() == this;
    }

    @Override
    public int damageDropped(final IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(final CreativeTabs group, final NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int data) {
        return this.getDefaultState()
                .withProperty(STAGE, data >> 3);
    }

    @Deprecated
    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(STAGE) << 3;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE, TYPE);
    }
}
