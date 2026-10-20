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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Milk filled cruet, from milking a cow with an empty cruet (see MixinEntityCow). Recipes using it leave the empty cruet behind.
 */
public final class GlassMilkCruetItem extends ComplexItem {

    public GlassMilkCruetItem() {
        super(new ResourceLocation(Almura.ID, "normal/ingredient/glass_milk_cruet"), "glass_milk_cruet");
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
        // Only holds a fluid when a mod registers a "milk" fluid (see Cruets)
        return new CruetFluidHandler(stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Milk Filled Container");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
