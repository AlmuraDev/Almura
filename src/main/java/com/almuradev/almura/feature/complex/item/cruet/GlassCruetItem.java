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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Empty cruet. Right click a water source to fill it (milking a cow fills it with milk, see MixinEntityCow); tanks and machines can
 * fill it through its fluid handler.
 */
public final class GlassCruetItem extends ComplexItem {

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

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new CruetFluidHandler(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack held = player.getHeldItem(hand);
        final RayTraceResult hit = this.rayTrace(world, player, true);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }

        final BlockPos pos = hit.getBlockPos();
        if (!world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos.offset(hit.sideHit), hit.sideHit, held)) {
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }

        // Takes the source block like a bucket does, through the cruet's fluid handler (plays the fill sound)
        final FluidActionResult result = FluidUtil.tryPickUpFluid(held, player, world, pos, hit.sideHit);
        if (!result.isSuccess()) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, Cruets.exchange(held, player, result.getResult()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Container");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
