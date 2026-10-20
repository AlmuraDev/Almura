/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.cruet;

import com.almuradev.almura.Almura;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * The glass cruets: an empty one, and one each filled with water and milk. A cruet holds one bucket's worth of fluid
 * ({@link #CAPACITY}): filling it takes a water source block, emptying it places one.
 */
public final class Cruets {

    public static final int CAPACITY = Fluid.BUCKET_VOLUME;

    static final ResourceLocation EMPTY = new ResourceLocation(Almura.ID, "normal/ingredient/glass_cruet");
    static final ResourceLocation WATER = new ResourceLocation(Almura.ID, "normal/ingredient/glass_water_cruet");
    static final ResourceLocation MILK = new ResourceLocation(Almura.ID, "normal/ingredient/glass_milk_cruet");

    /**
     * Mods register milk as a fluid named "milk"; without one, milk cruets don't hold a fluid and only work as an ingredient.
     */
    private static final String MILK_FLUID = "milk";

    public static ItemStack empty() {
        final Item item = ForgeRegistries.ITEMS.getValue(EMPTY);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Nullable
    static Fluid fluidIn(final Item item) {
        if (item instanceof GlassWaterCruetItem) {
            return FluidRegistry.WATER;
        }
        if (item instanceof GlassMilkCruetItem) {
            return FluidRegistry.getFluid(MILK_FLUID);
        }
        return null;
    }

    @Nullable
    static Item filledWith(final Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return ForgeRegistries.ITEMS.getValue(WATER);
        }
        final Fluid milk = FluidRegistry.getFluid(MILK_FLUID);
        if (milk != null && fluid == milk) {
            return ForgeRegistries.ITEMS.getValue(MILK);
        }
        return null;
    }

    /**
     * Swaps one cruet of the held stack for the result of filling/emptying it, like a bucket.
     *
     * @return The stack to keep in hand
     */
    static ItemStack exchange(final ItemStack held, final EntityPlayer player, final ItemStack result) {
        if (player.capabilities.isCreativeMode) {
            return held;
        }

        held.shrink(1);
        if (held.isEmpty()) {
            return result;
        }
        if (!player.inventory.addItemStackToInventory(result)) {
            player.dropItem(result, false);
        }
        return held;
    }

    private Cruets() {
    }
}
