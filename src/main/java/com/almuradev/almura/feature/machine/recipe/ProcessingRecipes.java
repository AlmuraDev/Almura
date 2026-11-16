/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.recipe;

import com.almuradev.almura.mixin.ShapedRecipeAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The recipes a processing machine can run, taken from the game's crafting recipes (Almura content, vanilla and other mods), so the machine
 * makes exactly what crafting would and picks up new recipes on its own, as 3.5's. Each machine decides which crafting recipes count, in
 * {@link #entryFor}. Server only: 26.3's clients don't have the recipes.
 */
public abstract class ProcessingRecipes {

    private @Nullable List<Entry> entries;
    private @Nullable RecipeManager entriesFrom;
    private int recipeCount = -1;

    /**
     * Turns a crafting recipe (its ingredients, without a shaped recipe's empty slots, and its result) into a machine recipe, or null if
     * this machine can't run it.
     */
    protected abstract @Nullable Entry entryFor(List<Ingredient> ingredients, ItemStack result);

    /**
     * @return what the machine can make from {@code input}, in the same order every time (the output selector refers to it by index). When
     * several recipes make the same output, only the one using the fewest inputs is kept.
     */
    public final List<ProcessingRecipe> recipesFor(final RecipeManager recipes, final ItemStack input) {
        if (input.isEmpty()) {
            return List.of();
        }
        final List<ProcessingRecipe> found = new ArrayList<>();
        for (final Entry entry : this.entries(recipes)) {
            if (!entry.input.test(input)) {
                continue;
            }
            boolean replaced = false;
            for (int i = 0; i < found.size(); i++) {
                final ProcessingRecipe existing = found.get(i);
                if (ItemStack.isSameItemSameComponents(existing.outputView(), entry.recipe.outputView())) {
                    if (entry.recipe.inputCount() < existing.inputCount()) {
                        found.set(i, entry.recipe);
                    }
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                found.add(entry.recipe);
            }
        }
        found.sort(Comparator.comparing(recipe -> BuiltInRegistries.ITEM.getKey(recipe.outputView().getItem()).toString()));
        return found;
    }

    public final boolean isInput(final RecipeManager recipes, final ItemStack input) {
        return !this.recipesFor(recipes, input).isEmpty();
    }

    private List<Entry> entries(final RecipeManager recipes) {
        // Made again when the recipes are reloaded (a new manager) or change
        final int count = recipes.getRecipes().size();
        if (this.entries == null || this.entriesFrom != recipes || count != this.recipeCount) {
            final List<Entry> found = new ArrayList<>();
            for (final RecipeHolder<?> holder : recipes.getRecipes()) {
                final Recipe<?> recipe = holder.value();
                final ItemStack result = result(recipe);
                if (result.isEmpty()) {
                    continue;
                }
                final List<Ingredient> ingredients = recipe.placementInfo().ingredients();
                if (ingredients.isEmpty()) {
                    continue;
                }
                final Entry entry = this.entryFor(ingredients, result);
                if (entry != null) {
                    found.add(entry);
                }
            }
            this.entries = found;
            this.entriesFrom = recipes;
            this.recipeCount = count;
        }
        return this.entries;
    }

    /**
     * A crafting table's shaped or shapeless recipe's result, as written; nothing for other recipes.
     */
    private static ItemStack result(final Recipe<?> recipe) {
        final ItemStackTemplate template;
        if (recipe instanceof final ShapedRecipe shaped) {
            template = ((ShapedRecipeAccessor) shaped).almura$result();
        } else if (recipe instanceof final ShapelessRecipe shapeless) {
            template = shapeless.result();
        } else {
            return ItemStack.EMPTY;
        }
        return template == null ? ItemStack.EMPTY : template.create();
    }

    protected static final class Entry {

        final Ingredient input;
        final ProcessingRecipe recipe;

        Entry(final Ingredient input, final int inputCount, final ItemStack output) {
            this.input = input;
            this.recipe = new ProcessingRecipe(inputCount, output.copy());
        }
    }
}
