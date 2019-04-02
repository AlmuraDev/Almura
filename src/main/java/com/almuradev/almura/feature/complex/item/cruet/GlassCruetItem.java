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
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.Sponge;

public final class GlassCruetItem extends ComplexItem {

    private final Block containedBlock = Blocks.AIR; // This will change based on the type of item.

    public GlassCruetItem() {
        super(new ResourceLocation(Almura.ID, "normal/ingredient/glass_cruet"), "glass_cruet");
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
        boolean containsAir = this.containedBlock == Blocks.AIR;
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, containsAir);

        if (raytraceresult == null) {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
                return new ActionResult<>(EnumActionResult.FAIL, itemstack);
            } else {
                if (!playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                } else {
                    IBlockState iblockstate = worldIn.getBlockState(blockpos);
                    Material material = iblockstate.getMaterial();

                    if (material == Material.WATER && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                        worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);

                        ItemStack water_cruet = GameRegistry.makeItemStack("almura:normal/ingredient/glass_water_cruet", 0, 1, null);

                        return new ActionResult<>(EnumActionResult.SUCCESS, this.fillCruet(itemstack, playerIn, water_cruet.getItem()));

                    } else {
                        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                    }
                }
            }
        }
    }

    private ItemStack fillCruet(ItemStack glass_cruet, EntityPlayer player, Item water_cruet) {
        if (player.capabilities.isCreativeMode) {
            return glass_cruet;
        } else {
            glass_cruet.shrink(1);

            if (glass_cruet.isEmpty()) {
                return new ItemStack(water_cruet);
            } else {
                if (!player.inventory.addItemStackToInventory(new ItemStack(water_cruet))) {
                    player.dropItem(new ItemStack(water_cruet), false);
                }

                return glass_cruet;
            }
        }
    }
}
