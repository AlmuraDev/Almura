/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.block.membershipexchange;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.block.ComplexBlock;
import com.almuradev.almura.feature.membership.MembershipHandler;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import javax.inject.Inject;

public final class MembershipExchange extends ComplexBlock {

    @Inject private static ServerNotificationManager serverNotificationManager;
    @Inject private static MembershipHandler membershipHandler;

    public MembershipExchange(ResourceLocation registryName, float hardness, float resistance) {
        super(Material.GROUND);
        this.setRegistryName(registryName);
        this.setTranslationKey(registryName.getPath().replace('/', '.'));
        this.setHardness(hardness);
        this.setResistance(resistance);

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":currency").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    protected boolean hasInvalidNeighbor(World worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer mcPlayer, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        }

        final Player player = (Player) mcPlayer;
        membershipHandler.requestClientGui(player);

       return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        this.onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!(world instanceof WorldServer) || ((WorldServer) world).isRemote) {
            return;
        }

        final ItemStack toDrop = new ItemStack(this, 1, 0);
        drops.add(toDrop);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        final ItemStack pickStack = new ItemStack(this, 1, 0);

        return pickStack;
    }
}
