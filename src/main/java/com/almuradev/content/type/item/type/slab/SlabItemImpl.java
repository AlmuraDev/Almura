/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab;

import com.almuradev.almura.asm.mixin.accessors.item.ItemAccessor;
import com.almuradev.almura.asm.mixin.accessors.item.ItemBlockAccessor;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.item.ItemTooltip;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

public final class SlabItemImpl extends ItemSlab implements SlabItem {
    private final ItemTooltip tooltip = new ItemTooltip.Impl(this);
    private final LazyBlockState doubleBlock;
    private final LazyBlockState singleBlock;

    SlabItemImpl(final SlabItemBuilder builder) {
        super(Blocks.AIR, null, null);
        ((ItemAccessor) (Object) this).accessor$setTabToDisplayOn(null);
        this.doubleBlock = builder.doubleBlock;
        this.singleBlock = builder.singleBlock;
        builder.fill(this);

        this.setHasSubtypes(false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> list, final ITooltipFlag flag) {
        this.checkAndDelegate();
        this.tooltip.render(list);
    }

    @Override
    public Block getBlock() {
        this.checkAndDelegate();
        return super.getBlock();
    }

    @Override
    public void getSubItems(final CreativeTabs group, final NonNullList<ItemStack> items) {
        this.checkAndDelegate();

        if (this.isInCreativeTab(group)) {
            this.getBlock().getSubBlocks(group, items);
        }
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        this.checkAndDelegate();

        final ItemStack item = player.getHeldItem(hand);
        if (item.isEmpty() || !player.canPlayerEdit(pos.offset(facing), facing, item)) {
            return EnumActionResult.FAIL;
        }

        final IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.singleBlock.get().getBlock()) {
            final BlockSlab.EnumBlockHalf half = state.getValue(BlockSlab.HALF);

            if (facing == EnumFacing.UP && half == BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN && half == BlockSlab.EnumBlockHalf.TOP) {
                final IBlockState doubleState = this.doubleBlock.get();
                final AxisAlignedBB aabb = doubleState.getCollisionBoundingBox(world, pos);

                if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, doubleState, 11)) {
                    final SoundType sounds = this.doubleBlock.get().getBlock().getSoundType(doubleState, world, pos, player);
                    world.playSound(player, pos, sounds.getPlaceSound(), SoundCategory.BLOCKS, (sounds.getVolume() + 1.0F) / 2.0F, sounds.getPitch() * 0.8F);

                    item.shrink(1);

                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, item);
                    }
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return this.tryPlaceSingle(world, player, hand, item, pos, facing, hitX, hitY, hitZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(final World worldIn, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        this.checkAndDelegate();

        final IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() == this.singleBlock.get().getBlock()) {
            final boolean topHalf = state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
            if (side == EnumFacing.UP && !topHalf || side == EnumFacing.DOWN && topHalf) {
                return true;
            }
        }
        return worldIn.getBlockState(pos.offset(side)).getBlock() == this.singleBlock.get().getBlock() || super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
    }

    private EnumActionResult tryPlaceSingle(final World world, final EntityPlayer player, final EnumHand hand, final ItemStack itemStack, BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        IBlockState previousState = world.getBlockState(pos);
        final Block previousBlock = previousState.getBlock();

        if (!previousBlock.isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }

        if (!itemStack.isEmpty() && player.canPlayerEdit(pos, facing, itemStack) && world.mayPlace(this.singleSlab, pos, false, facing, player)) {
            final IBlockState singleState = this.singleSlab.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, 0, player, hand);

            if (world.setBlockState(pos, singleState, 11)) {
                previousState = world.getBlockState(pos);

                if (singleState.equals(previousState)) {
                    this.getBlock().onBlockPlacedBy(world, pos, singleState, player, itemStack);

                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemStack);
                    }

                    final SoundType sounds = singleState.getBlock().getSoundType(singleState, world, pos, player);
                    world.playSound(player, pos, sounds.getPlaceSound(), SoundCategory.BLOCKS, (sounds.getVolume() + 1.0F) / 2.0F, sounds.getPitch() * 0.8F);
                    itemStack.shrink(1);

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.FAIL;
    }

    @Override
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState newState) {
        this.checkAndDelegate();

        if (!world.setBlockState(pos, newState, 11)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.getBlock()) {
            setTileEntityNBT(world, player, pos, stack);
            this.getBlock().onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
            }
        }

        return true;
    }

    private void checkAndDelegate() {
        if (this.block == Blocks.AIR || this.singleSlab == null || this.doubleSlab == null) {
            this.singleSlab = (BlockSlab) this.singleBlock.get().getBlock();
            this.doubleSlab = (BlockSlab) this.doubleBlock.get().getBlock();
            ((ItemBlockAccessor) (Object) this).accessor$setBlock (this.singleSlab);
        }
    }
}
