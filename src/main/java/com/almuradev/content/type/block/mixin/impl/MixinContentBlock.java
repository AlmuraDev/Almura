/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.DelegateSet;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.AbstractBlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinContentBlock;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.flower.FlowerBlockImpl;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockImpl;
import com.almuradev.content.type.block.type.leaf.LeafBlockImpl;
import com.almuradev.content.type.block.type.log.LogBlockImpl;
import com.almuradev.content.type.block.type.normal.NormalBlockImpl;
import com.almuradev.content.type.block.type.sapling.SaplingBlockImpl;
import com.almuradev.content.type.block.type.slab.SlabBlockImpl;
import com.almuradev.content.type.block.type.stair.StairBlockImpl;
import com.almuradev.content.type.block.util.BlockUtil;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.item.mixin.EffectiveOn;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin({
    CropBlockImpl.class,
    FlowerBlockImpl.class,
    HorizontalBlockImpl.class,
    LeafBlockImpl.class,
    LogBlockImpl.class,
    NormalBlockImpl.class,
    SaplingBlockImpl.class,
    SlabBlockImpl.class,
    StairBlockImpl.class
})
public abstract class MixinContentBlock extends MixinBlock implements ContentBlock, EffectiveOn, IMixinContentBlock, IMixinLazyItemGroup {
    @Nullable private Delegate<ItemGroup> lazyItemGroup;
    private BlockRenderLayer renderLayer = BlockRenderLayer.CUTOUT_MIPPED;
    private @Nullable DelegateSet<ItemType, Item> effectiveTools;

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
    public CreativeTabs getCreativeTab() {
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
    public void setRenderLayer(BlockRenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return ((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).blockFaceShape;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        final IBlockState state = world.getBlockState(pos);
        return ((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).flammability.orElse(0);
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        final IBlockState state = world.getBlockState(pos);
        return ((AbstractBlockStateDefinition<?, ?, ?>) this.definition(state)).fireSpreadSpeed.orElse(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.renderLayer;
    }

    @Override
    public boolean effectiveTool(final Item item) {
        return this.effectiveTools != null && this.effectiveTools.contains(item);
    }

    @Override
    public void effectiveTools(final DelegateSet<ItemType, Item> effectiveTools) {
        this.effectiveTools = effectiveTools;
    }

    // Almura Start - Handle drops
    @Override
    public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, final ItemStack stack) {
        BlockUtil.handleHarvest(this, world, player, pos, state, te, stack);
    }

    @Override
    public void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        BlockUtil.handleDropBlockAsItemWithChance(this, world, pos, state, chance, fortune);
    }
    // Almura End - Handle drops
}
