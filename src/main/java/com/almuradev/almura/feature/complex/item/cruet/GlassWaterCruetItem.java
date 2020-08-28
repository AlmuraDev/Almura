/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.cruet;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.item.ComplexItem;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.List;

import javax.annotation.Nullable;

public final class GlassWaterCruetItem extends ComplexItem {

    private final Block containedBlock = Blocks.WATER; // This will change based on the type of item.

    public GlassWaterCruetItem() {
        super(new ResourceLocation(Almura.ID, "normal/ingredient/glass_water_cruet"), "glass_water_cruet");
        this.setMaxStackSize(64);
        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":ingredient").ifPresent((itemGroup) -> this.setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, false);

        if (raytraceresult == null) {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else {

            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
                return new ActionResult<>(EnumActionResult.FAIL, itemstack);
            } else {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                } else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1)) {
                    if (playerIn instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
                    }
                    ItemStack glass_cruet = GameRegistry.makeItemStack("almura:normal/ingredient/glass_cruet", 0, 1, null);

                    return new ActionResult<>(EnumActionResult.SUCCESS, this.emptyCruet(itemstack, playerIn, glass_cruet.getItem()));
                } else {
                    return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                }
            }
        }
    }

    private ItemStack emptyCruet(ItemStack glass_water_cruet, EntityPlayer player, Item glass_cruet) {
        if (player.capabilities.isCreativeMode) {
            return glass_water_cruet;
        } else {
            glass_water_cruet.shrink(1);

            if (glass_water_cruet.isEmpty()) {
                return new ItemStack(glass_cruet);
            } else {
                if (!player.inventory.addItemStackToInventory(new ItemStack(glass_cruet))) {
                    player.dropItem(new ItemStack(glass_cruet), false);
                }

                return glass_water_cruet;
            }
        }
    }

    private boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
        IBlockState iblockstate = worldIn.getBlockState(posIn);
        Material material = iblockstate.getMaterial();
        boolean solidMaterial = !material.isSolid();
        boolean isReplaceable = iblockstate.getBlock().isReplaceable(worldIn, posIn);

        if (!worldIn.isAirBlock(posIn) && !solidMaterial && !isReplaceable) {
            return false;
        } else {
            if (!worldIn.isRemote && (solidMaterial || isReplaceable) && !material.isLiquid()) {
                worldIn.destroyBlock(posIn, true);
            }

            SoundEvent soundevent = SoundEvents.ITEM_BUCKET_EMPTY;
            worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
        }

        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Water Filled Container");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
