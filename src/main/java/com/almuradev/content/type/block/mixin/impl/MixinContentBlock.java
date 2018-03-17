/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.AbstractBlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinContentBlock;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockImpl;
import com.almuradev.content.type.block.type.leaf.LeafBlockImpl;
import com.almuradev.content.type.block.type.log.LogBlockImpl;
import com.almuradev.content.type.block.type.normal.NormalBlockImpl;
import com.almuradev.content.type.block.type.sapling.SaplingBlockImpl;
import com.almuradev.content.type.block.type.slab.SlabBlockImpl;
import com.almuradev.content.type.block.type.stair.StairBlockImpl;
import com.almuradev.content.type.block.util.BlockUtil;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin({
    CropBlockImpl.class,
    HorizontalBlockImpl.class,
    LeafBlockImpl.class,
    LogBlockImpl.class,
    NormalBlockImpl.class,
    SaplingBlockImpl.class,
    SlabBlockImpl.class,
    StairBlockImpl.class
})
public abstract class MixinContentBlock extends MixinBlock implements ContentBlock, IMixinContentBlock, IMixinLazyItemGroup {
    @Nullable private Delegate<ItemGroup> lazyItemGroup;

    @Override
    public Optional<ItemGroup> itemGroup() {
        if (this.displayOnCreativeTab != null) {
            return Optional.of((ItemGroup) this.displayOnCreativeTab);
        }
        if (this.lazyItemGroup == null) {
            return Optional.empty();
        }
        this.displayOnCreativeTab = (CreativeTabs) this.lazyItemGroup.get();
        return Optional.ofNullable((ItemGroup) this.displayOnCreativeTab);
    }

    @Override
    public void itemGroup(final Delegate<ItemGroup> group) {
        this.lazyItemGroup = group;
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return (CreativeTabs) this.itemGroup().orElse(null);
    }

    @Override
    public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, @Nullable final Entity entity) {
        return (SoundType) this.soundGroup(state).orElse((BlockSoundGroup) super.getSoundType(state, world, pos, entity));
    }

    @Override
    public Optional<BlockSoundGroup> soundGroup(final IBlockState state) {
        return Delegate.optional(((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).sound);
    }

    @Nullable
    @Override
    public BlockDestroyAction destroyAction(final IBlockState state) {
        return Delegate.get(((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).destroyAction);
    }

    @Override
    public ThreadLocal<EntityPlayer> getHarvesters() {
        return this.harvesters;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return ((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).blockFaceShape;
    }

    // Almura Start - Handle drops from Break
    @Override
    public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, final ItemStack stack) {
        BlockUtil.handleHarvest(this, world, player, pos, state, te, stack);
    }

    @Override
    public void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        BlockUtil.handleDropBlockAsItemWithChance(this, world, pos, state, chance, fortune);
    }
    // Almura End - Handle drops from Break
}
