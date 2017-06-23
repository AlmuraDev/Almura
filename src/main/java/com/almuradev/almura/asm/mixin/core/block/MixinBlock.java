/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import javax.annotation.Nullable;

// Makes all blocks BuildableBlockTypes (so they can be used in Almura's framework)
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements BuildableBlockType {

    @Nullable
    @Shadow public CreativeTabs displayOnCreativeTab;

    @Shadow protected ThreadLocal<EntityPlayer> harvesters;

    @Shadow public abstract void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te,
            ItemStack stack);

    @Shadow public void dropBlockAsItem(World worldIn, BlockPos pos, IBlockState state, int fortune)
    {

    }

    @Shadow public abstract void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune);

    @Override
    public Optional<ItemGroup> getItemGroup() {
        return Optional.ofNullable((ItemGroup) this.displayOnCreativeTab);
    }
}
