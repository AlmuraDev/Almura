/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A furnace-like machine that turns crops and other ingredients into the seeds a crafting table would give for them. Faces the player that
 * placed it and switches to its active textures while working.
 */
public final class SeedProcessorBlock extends BlockContainer {

    public static final ResourceLocation ID = new ResourceLocation(Almura.ID, "machine/seed_processor");
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    @Inject private static Almura plugin;

    public SeedProcessorBlock() {
        super(Material.IRON);

        this.setRegistryName(ID);
        this.setTranslationKey(ID.getPath().replace('/', '.'));
        this.setHardness(3.5F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH).withProperty(ACTIVE, false));

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":agriculture").ifPresent(group -> this.setCreativeTab((CreativeTabs) group));
    }

    /**
     * Switches the active textures on or off, keeping the facing (the tile entity is kept, see {@link SeedProcessorTileEntity#shouldRefresh}).
     */
    static void setActive(final World world, final BlockPos pos, final boolean active) {
        final IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof SeedProcessorBlock && state.getValue(ACTIVE) != active) {
            world.setBlockState(pos, state.withProperty(ACTIVE, active), 3);
        }
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            player.openGui(plugin, AlmuraGuiHandler.SEED_PROCESSOR, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new SeedProcessorTileEntity();
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof SeedProcessorTileEntity) {
            final IItemHandler inventory = ((SeedProcessorTileEntity) te).getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                final ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy());
                }
            }
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(final IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        return te instanceof SeedProcessorTileEntity ? ItemHandlerHelper.calcRedstoneFromInventory(((SeedProcessorTileEntity) te).getInventory())
                : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING, ACTIVE);
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY,
            final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState()
                .withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(ACTIVE, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex() | (state.getValue(ACTIVE) ? 4 : 0);
    }
}
