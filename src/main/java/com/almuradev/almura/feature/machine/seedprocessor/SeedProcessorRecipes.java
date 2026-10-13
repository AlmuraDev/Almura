/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import com.almuradev.almura.feature.machine.processing.ProcessingRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * What the seed processor makes: every crafting recipe (from Almura content, vanilla or other mods) that turns a single ingredient into
 * seeds, i.e. an output item whose id contains "seed". The machine gives the same output a crafting table would.
 */
public final class SeedProcessorRecipes extends ProcessingRecipes {

    public static final SeedProcessorRecipes INSTANCE = new SeedProcessorRecipes();

    private SeedProcessorRecipes() {
    }

    @Nullable
    @Override
    protected Entry entryFor(final IRecipe recipe) {
        final ItemStack output = recipe.getRecipeOutput();
        if (!isSeed(output)) {
            return null;
        }
        final List<Ingredient> ingredients = ingredients(recipe);
        return ingredients.size() == 1 ? new Entry(ingredients.get(0), 1, output) : null;
    }

    private static boolean isSeed(final ItemStack stack) {
        final ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && id.getPath().toLowerCase(Locale.ENGLISH).contains("seed");
    }
}
