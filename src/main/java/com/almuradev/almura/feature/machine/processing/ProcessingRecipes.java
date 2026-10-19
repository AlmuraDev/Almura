/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * The recipes a processing machine can run, taken from the game's crafting recipes (Almura content, vanilla and other mods), so the machine
 * makes exactly what crafting would and picks up new recipes automatically. Each machine type decides which crafting recipes count, in
 * {@link #entryFor}.
 */
public abstract class ProcessingRecipes {

    @Nullable private List<Entry> entries;
    private int recipeCount = -1;

    /**
     * Turns a crafting recipe into a machine recipe, or null if this machine can't run it.
     */
    @Nullable
    protected abstract Entry entryFor(IRecipe recipe);

    /**
     * Adds recipes that don't come from crafting (e.g. the wash plant's), alongside those from {@link #entryFor}.
     */
    protected void addFixedEntries(final List<Entry> entries) {
    }

    /**
     * @return the outputs the player picks between up front, the same whatever the input (e.g. the wash plant's gravel / gold / iron
     * modes), in {@link #recipesFor} order; empty when the choice depends on the input instead
     */
    public List<ProcessingRecipe> modes() {
        return Collections.emptyList();
    }

    /**
     * @return what the GUI's selector chooses between: the {@link #modes} if there are any, otherwise the input's recipes
     */
    public final List<ProcessingRecipe> selectable(final ItemStack input) {
        final List<ProcessingRecipe> modes = this.modes();
        return modes.isEmpty() ? this.recipesFor(input) : modes;
    }

    /**
     * @return what the machine can make from {@code input}: the same list in the same order on the client and the server (the GUI's output
     * selector refers to it by index). When several recipes make the same output, only the one using the fewest inputs is kept.
     */
    public final List<ProcessingRecipe> recipesFor(final ItemStack input) {
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ProcessingRecipe> found = new ArrayList<>();
        for (final Entry entry : this.entries()) {
            if (!entry.input.apply(input)) {
                continue;
            }
            boolean replaced = false;
            for (int i = 0; i < found.size(); i++) {
                final ProcessingRecipe existing = found.get(i);
                if (sameItem(existing.outputView(), entry.recipe.outputView())) {
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

        // Stable order on both sides, independent of registry iteration order
        found.sort(Comparator.comparing((ProcessingRecipe recipe) -> String.valueOf(recipe.outputView().getItem().getRegistryName()))
                .thenComparingInt(recipe -> recipe.outputView().getMetadata()));
        return found;
    }

    public final boolean isInput(final ItemStack input) {
        return !this.recipesFor(input).isEmpty();
    }

    private List<Entry> entries() {
        // Rebuilt if recipes are added or replaced after the first lookup (e.g. by another mod or a reload)
        final int count = ForgeRegistries.RECIPES.getValuesCollection().size();
        if (this.entries == null || count != this.recipeCount) {
            final List<Entry> found = new ArrayList<>();
            for (final IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
                if (recipe.getRecipeOutput().isEmpty()) {
                    continue;
                }
                final Entry entry = this.entryFor(recipe);
                if (entry != null) {
                    found.add(entry);
                }
            }
            this.addFixedEntries(found);
            this.entries = found;
            this.recipeCount = count;
        }
        return this.entries;
    }

    /**
     * The recipe's ingredients, without the empty slots of shaped recipes.
     */
    protected static List<Ingredient> ingredients(final IRecipe recipe) {
        final List<Ingredient> ingredients = new ArrayList<>();
        for (final Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient != Ingredient.EMPTY && ingredient.getMatchingStacks().length > 0) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    /**
     * Whether two ingredients accept exactly the same items.
     */
    protected static boolean sameIngredient(final Ingredient a, final Ingredient b) {
        final ItemStack[] first = a.getMatchingStacks();
        final ItemStack[] second = b.getMatchingStacks();
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) {
            if (!sameItem(first[i], second[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameItem(final ItemStack a, final ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata() && ItemStack.areItemStackTagsEqual(a, b);
    }

    protected static final class Entry {

        final Ingredient input;
        final ProcessingRecipe recipe;

        public Entry(final Ingredient input, final int inputCount, final ItemStack output) {
            this(input, inputCount, output, 1);
        }

        public Entry(final Ingredient input, final int inputCount, final ItemStack output, final double chance) {
            this.input = input;
            this.recipe = new ProcessingRecipe(inputCount, output.copy(), chance);
        }
    }
}
