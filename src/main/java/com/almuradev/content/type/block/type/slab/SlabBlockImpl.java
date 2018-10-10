/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinition;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class SlabBlockImpl extends BlockSlab implements SlabBlock {
    private final SlabBlockStateDefinition definition;
    private final boolean isDouble;
    private LazyBlockState single;

    SlabBlockImpl(final SlabBlockBuilder builder) {
        // TODO This too early, fix this better
        super((Material) builder.material.get());
        this.displayOnCreativeTab = null;
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.isDouble = builder.isDouble;
        this.fullBlock = this.isDouble;
        this.single = builder.single;

        if (!this.isDouble()) {
            this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, EnumBlockHalf.BOTTOM));
        } else {
            this.blockState = this.createBlockState();
            this.setDefaultState(this.blockState.getBaseState());
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return !this.isDouble() ? new BlockStateContainer(this, HALF) : new BlockStateContainer(this);
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        if (this.isDouble()) {
            return new ItemStack(this.single.get().getBlock(), 1);
        } else {
            return super.getPickBlock(state, target, world, pos, player);
        }
    }

    @Override
    public SlabBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public String getTranslationKey(final int meta) {
        return this.getTranslationKey();
    }

    @Override
    public boolean isDouble() {
        return this.isDouble;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        //. Does not matter, goes away in 1.13 and we don't use it anyways
        return null;
    }

    @Override
    public Comparable<?> getTypeForItem(final ItemStack stack) {
        //. Does not matter, goes away in 1.13 and we don't use it anyways
        return null;
    }

    @Override
    public IBlockState getStateFromMeta(final int legacy) {
        IBlockState state = this.getDefaultState();
        if (!this.isDouble()) {
            state = state.withProperty(HALF, legacy == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }
        return state;
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) ? 1 : 0;
    }
}
