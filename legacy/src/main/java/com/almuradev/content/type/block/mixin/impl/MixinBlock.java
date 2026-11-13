/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinBlock;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(value = Block.class, priority = 998)
public abstract class MixinBlock implements ContentBlock, IMixinBlock {

    @Shadow public abstract CreativeTabs getCreativeTab();
    @Shadow protected SoundType blockSoundType;
    @Shadow(remap = false) protected ThreadLocal<EntityPlayer> harvesters;
    @Shadow public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, final ItemStack stack) { }
    @Shadow public void dropBlockAsItem(final World world, final BlockPos pos, final IBlockState state, final int fortune) { }
    @Shadow public void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune) { }
    @Shadow(remap = false) public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, @Nullable final Entity
            entity) { return null; }
    @Shadow public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) { return null; }
    @Shadow(remap = false) public abstract int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face);
    @Shadow(remap = false) public abstract int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face);
    @SideOnly(Side.CLIENT)
    @Shadow public abstract BlockRenderLayer getRenderLayer();

    @Override
    public Optional<ItemGroup> itemGroup() {
        return Optional.ofNullable((ItemGroup) ((BlockAccessor) this).accessor$getDisplayOnCreativeTab());
    }

    private boolean dropsFromDecay = false;

    @Override
    public boolean dropsFromDecay() {
        return this.dropsFromDecay;
    }

    @Override
    public void setDropsFromDecay(final boolean dropsFromDecay) {
        this.dropsFromDecay = dropsFromDecay;
    }
}
