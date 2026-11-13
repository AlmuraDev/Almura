/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Recipes for a machine that replaces crafting tools (e.g. the kitchen slicer for the knife and the grater): crafting recipes made of one of
 * the tools plus one or more of the same item. The machine uses that many items and makes the recipe's result; the tool itself isn't needed
 * or worn. Recipes with several different ingredients (besides the tool) don't fit a single input slot, so they stay crafting only.
 */
public final class ToolRecipes extends ProcessingRecipes {

    private final List<ResourceLocation> toolIds = new ArrayList<>();
    @Nullable private List<ItemStack> tools;

    /**
     * @param toolIds the crafting tools this machine replaces
     */
    public ToolRecipes(final String... toolIds) {
        for (final String toolId : toolIds) {
            this.toolIds.add(new ResourceLocation(toolId));
        }
    }

    @Nullable
    @Override
    protected Entry entryFor(final IRecipe recipe) {
        final List<ItemStack> tools = this.tools();
        if (tools.isEmpty()) {
            return null;
        }

        Ingredient input = null;
        int inputCount = 0;
        boolean hasTool = false;
        for (final Ingredient ingredient : ingredients(recipe)) {
            if (!hasTool && acceptsAny(ingredient, tools)) {
                hasTool = true;
            } else if (input == null) {
                input = ingredient;
                inputCount = 1;
            } else if (sameIngredient(input, ingredient)) {
                inputCount++;
            } else {
                // A second, different ingredient
                return null;
            }
        }

        // The input being a tool itself (e.g. tool + tool) isn't a processing recipe
        if (!hasTool || input == null || acceptsAny(input, tools)) {
            return null;
        }
        return new Entry(input, inputCount, recipe.getRecipeOutput());
    }

    private static boolean acceptsAny(final Ingredient ingredient, final List<ItemStack> stacks) {
        for (final ItemStack stack : stacks) {
            if (ingredient.apply(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The tools as item stacks, or nothing until they're all registered.
     */
    private List<ItemStack> tools() {
        if (this.tools == null) {
            final List<ItemStack> found = new ArrayList<>();
            for (final ResourceLocation toolId : this.toolIds) {
                final Item item = ForgeRegistries.ITEMS.getValue(toolId);
                if (item == null) {
                    // Not registered (yet): don't remember the miss
                    return Collections.emptyList();
                }
                found.add(new ItemStack(item));
            }
            this.tools = found;
        }
        return this.tools;
    }
}
