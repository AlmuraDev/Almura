/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.MultiSlotItemHandler;
import com.almuradev.almura.shared.tileentity.MultiSlotTileEntity;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public final class ContainerBlockImpl extends BlockContainer implements ContainerBlock {
    private final ContainerBlockStateDefinition definition;
    private final int limit;
    private final int slotAmount;

    ContainerBlockImpl(final ContainerBlockBuilder builder) {
        this(builder, builder.singleState());
        builder.fill(this);
    }

    private ContainerBlockImpl(final ContainerBlockBuilder builder, final ContainerBlockStateDefinition definition) {
        super((Material) builder.material.get());
        this.definition = definition;
        this.limit = builder.limit;
        this.slotAmount = builder.slots;
    }


    @Override
    public ContainerBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        player.openGui(Almura.ID, 0, world, pos.getX(), pos.getY(), pos.getZ());

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
    public TileEntity createNewTileEntity(final World world, final int meta) {
        // This Class originally targetted ContainerBlockEntity, this was changed on 10/3/23 by Dockter at the request
        // of Zidane to make use of the already implemented functionality of MultiSlotTileInventory

        //return new ContainerBlockEntity(this.limit);

        final MultiSlotTileEntity mte = new MultiSlotTileEntity(this.limit);

        final IMultiSlotItemHandler itemHandler = (MultiSlotItemHandler) mte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (this.getSlotCount() > itemHandler.getSlots()) {
            itemHandler.resize(this.getSlotCount());
        }

        return mte;
    }

    public int getSlotCount() {
        // Fail-safe to prevent GUI issues
        if (this.slotAmount > 54)
            return 54;
        if (this.slotAmount < 9)
            return 9;

        return this.slotAmount;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        MultiSlotTileEntity mte = (MultiSlotTileEntity)worldIn.getTileEntity(pos);
        final IMultiSlotItemHandler itemHandler = (MultiSlotItemHandler) mte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (mte instanceof MultiSlotTileEntity) {
            for (int i = 0; i < itemHandler.getSlots(); ++i) {
                ItemStack itemstack = itemHandler.getStackInSlot(i);

                if (!itemstack.isEmpty()) {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                }
            }

            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }
}
