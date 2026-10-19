/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.washplant;

import com.almuradev.almura.feature.machine.processing.ProcessingRecipe;
import com.almuradev.almura.feature.machine.processing.ProcessingRecipes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.List;

import javax.annotation.Nullable;

/**
 * The wash plant's three modes, the same for stone and cobblestone: 3 gravel every time, or a chance of a gold or iron nugget. The input
 * is used up whether or not a nugget comes out.
 */
public final class WashPlantRecipes extends ProcessingRecipes {

    public static final WashPlantRecipes INSTANCE = new WashPlantRecipes();

    /** Chance of a nugget per stone or cobblestone washed, the same as the old Paydirt wash plant gave for cobblestone. */
    public static final double NUGGET_CHANCE = 0.02;
    private static final int GRAVEL_PER_WASH = 3;

    private WashPlantRecipes() {
    }

    @Nullable
    @Override
    protected Entry entryFor(final IRecipe recipe) {
        // Nothing comes from crafting recipes
        return null;
    }

    @Override
    protected void addFixedEntries(final List<Entry> entries) {
        final Ingredient input = Ingredient.fromStacks(new ItemStack(Blocks.STONE), new ItemStack(Blocks.COBBLESTONE));
        entries.add(new Entry(input, 1, new ItemStack(Blocks.GRAVEL, GRAVEL_PER_WASH)));
        entries.add(new Entry(input, 1, new ItemStack(Items.GOLD_NUGGET), NUGGET_CHANCE));
        entries.add(new Entry(input, 1, new ItemStack(Items.IRON_NUGGET), NUGGET_CHANCE));
    }

    @Override
    public List<ProcessingRecipe> modes() {
        // Every input has all three, in the same order
        return this.recipesFor(new ItemStack(Blocks.COBBLESTONE));
    }
}
