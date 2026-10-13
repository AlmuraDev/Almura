/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.Almura;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * The block of a processing machine (see {@link MachineType}). Faces the player that placed it, opens the machine's GUI, switches to its
 * active textures while working, drops its contents when broken and gives comparator output.
 */
public final class ProcessingBlock extends BlockContainer {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    // Hitbox of machines with a shaped model (fits the kitchen slicer, which is about 0.8 blocks tall; the same in every facing)
    private static final AxisAlignedBB CUSTOM_MODEL_BOX = new AxisAlignedBB(0.04D, 0.0D, 0.04D, 0.96D, 0.8D, 0.96D);

    @Inject private static Almura plugin;

    private final MachineType type;

    public ProcessingBlock(final MachineType type) {
        super(Material.IRON);
        this.type = type;
        if (type.customModel()) {
            // Block's constructor worked these out from isOpaqueCube() before the type was set (as a full cube): correct them
            this.fullBlock = false;
            this.lightOpacity = 0;
        }

        this.setRegistryName(type.id());
        this.setTranslationKey(type.id().getPath().replace('/', '.'));
        this.setHardness(3.5F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH).withProperty(ACTIVE, false));

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":agriculture").ifPresent(group -> this.setCreativeTab((CreativeTabs) group));
    }

    public MachineType type() {
        return this.type;
    }

    /**
     * Switches the active textures on or off, keeping the facing (the tile entity is kept, see {@link ProcessingTileEntity#shouldRefresh}).
     */
    static void setActive(final World world, final BlockPos pos, final boolean active) {
        final IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ProcessingBlock && state.getValue(ACTIVE) != active) {
            world.setBlockState(pos, state.withProperty(ACTIVE, active), 3);
        }
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            player.openGui(plugin, this.type.guiId(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return this.type.createTileEntity();
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof ProcessingTileEntity) {
            final IItemHandler inventory = ((ProcessingTileEntity) te).getInventory();
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
        return te instanceof ProcessingTileEntity ? ItemHandlerHelper.calcRedstoneFromInventory(((ProcessingTileEntity) te).getInventory()) : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    // Machines with a shaped (OBJ) model aren't solid cubes: neighbours keep their faces, and the hitbox fits the model

    /**
     * Null safe: Block's constructor calls isOpaqueCube() before this block's constructor has set the type.
     */
    private boolean hasCustomModel() {
        return this.type != null && this.type.customModel();
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return !this.hasCustomModel();
    }

    @Deprecated
    @Override
    public boolean isFullCube(final IBlockState state) {
        return !this.hasCustomModel();
    }

    @Deprecated
    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess world, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return this.hasCustomModel() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return this.hasCustomModel() ? CUSTOM_MODEL_BOX : FULL_BLOCK_AABB;
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
