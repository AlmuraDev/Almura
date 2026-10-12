/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * What the seed processor makes: every crafting recipe (from Almura content, vanilla or other mods) that turns a single ingredient into
 * seeds, i.e. an output item whose id contains "seed". The machine gives the same output a crafting table would.
 */
public final class SeedProcessorRecipes {

    @Nullable private static List<Entry> recipes;
    private static int recipeCount = -1;

    private SeedProcessorRecipes() {
    }

    /**
     * @return a copy of the seeds made from {@code input}, or {@link ItemStack#EMPTY} if it isn't a seed processor input
     */
    public static ItemStack outputFor(final ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (final Entry entry : recipes()) {
            if (entry.input.apply(input)) {
                return entry.output.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isInput(final ItemStack input) {
        return !outputFor(input).isEmpty();
    }

    private static List<Entry> recipes() {
        // Rebuilt if recipes are added or replaced after the first lookup (e.g. by another mod or a reload)
        final int count = ForgeRegistries.RECIPES.getValuesCollection().size();
        if (recipes == null || count != recipeCount) {
            final List<Entry> found = new ArrayList<>();
            for (final IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
                final ItemStack output = recipe.getRecipeOutput();
                if (output.isEmpty() || !isSeed(output)) {
                    continue;
                }

                final Ingredient ingredient = singleIngredient(recipe);
                if (ingredient != null) {
                    found.add(new Entry(ingredient, output.copy()));
                }
            }
            recipes = Collections.unmodifiableList(found);
            recipeCount = count;
        }
        return recipes;
    }

    private static boolean isSeed(final ItemStack stack) {
        final ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && id.getPath().toLowerCase(Locale.ENGLISH).contains("seed");
    }

    /**
     * @return the recipe's only ingredient, or null if it uses more (or fewer) than one item
     */
    @Nullable
    private static Ingredient singleIngredient(final IRecipe recipe) {
        Ingredient single = null;
        for (final Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient == Ingredient.EMPTY || ingredient.getMatchingStacks().length == 0) {
                continue;
            }
            if (single != null) {
                return null;
            }
            single = ingredient;
        }
        return single;
    }

    private static final class Entry {

        final Ingredient input;
        final ItemStack output;

        Entry(final Ingredient input, final ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }
}
