/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.MultiSlotItemHandler;
import com.almuradev.almura.shared.tileentity.MultiSlotTileEntity;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;
import javax.inject.Inject;


public final class StorageBlock extends BlockContainer {

    @Inject
    private static Almura plugin;

    private final int slotAmount;

    public StorageBlock(ResourceLocation registryName, int slotAmount) {
        super(Material.GROUND, MapColor.BROWN);

        this.setRegistryName(registryName);
        this.setTranslationKey(registryName.getPath().replace('/', '.'));
        this.setHardness(6.0F);
        this.setResistance(2000.0F);

        this.slotAmount = slotAmount;

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":storage").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        }

        player.openGui(plugin, 0, world, pos.getX(), pos.getY(), pos.getZ());

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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        final MultiSlotTileEntity mte = new MultiSlotTileEntity();
        final IMultiSlotItemHandler itemHandler = (MultiSlotItemHandler) mte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (this.slotAmount > itemHandler.getSlots()) {
            itemHandler.resize(this.slotAmount);
        }

        return mte;
    }

    public int getSlotCount() {
        return this.slotAmount;
    }
}
