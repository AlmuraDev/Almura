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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Water filled cruet. Right click to place the water (not in dimensions where water evaporates, like the Nether); tanks and machines
 * can empty it through its fluid handler. Recipes using it leave the empty cruet behind.
 */
public final class GlassWaterCruetItem extends ComplexItem {

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

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(final ItemStack stack) {
        return Cruets.empty();
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new CruetFluidHandler(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack held = player.getHeldItem(hand);
        final RayTraceResult hit = this.rayTrace(world, player, false);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }

        final BlockPos clicked = hit.getBlockPos();
        if (!world.isBlockModifiable(player, clicked)) {
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }

        final boolean replaceable = world.getBlockState(clicked).getBlock().isReplaceable(world, clicked);
        final BlockPos target = replaceable && hit.sideHit == EnumFacing.UP ? clicked : clicked.offset(hit.sideHit);
        if (!player.canPlayerEdit(target, hit.sideHit, held)) {
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }

        // Places the water through the cruet's fluid handler (handles evaporating dimensions and the empty sound)
        final FluidActionResult result = FluidUtil.tryPlaceFluid(player, world, target, held, new FluidStack(FluidRegistry.WATER, Cruets.CAPACITY));
        if (!result.isSuccess()) {
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }

        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, target, held);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, Cruets.exchange(held, player, result.getResult()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Water Filled Container");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
