/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements ContentBlockType {

    @Nullable @Shadow public CreativeTabs displayOnCreativeTab;
    @Shadow protected SoundType blockSoundType;
    @Shadow(remap = false) protected ThreadLocal<EntityPlayer> harvesters;
    @Shadow public abstract void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, final ItemStack stack);
    @Shadow public void dropBlockAsItem(final World world, final BlockPos pos, final IBlockState state, final int fortune) { }
    @Shadow public abstract void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune);
    @Shadow(remap = false) public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, @Nullable final Entity
            entity) { return null; }

    @Override
    public Optional<ItemGroup> itemGroup() {
        return Optional.ofNullable((ItemGroup) this.displayOnCreativeTab);
    }
}
