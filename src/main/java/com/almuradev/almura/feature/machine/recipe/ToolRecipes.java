/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Recipes for a machine that replaces crafting tools (the kitchen slicer for the knife and the grater, the grinder for the grinder), as
 * 3.5's: crafting recipes made of one of the tools plus one or more of the same item. The machine uses that many items and makes the
 * recipe's result; the tool itself isn't needed or worn. Recipes with several different ingredients (besides the tool) don't fit its one
 * input slot, so they stay crafting only.
 */
public final class ToolRecipes extends ProcessingRecipes {

    private final List<Identifier> toolIds = new ArrayList<>();

    /**
     * @param toolIds the crafting tools this machine replaces
     */
    public ToolRecipes(final String... toolIds) {
        for (final String toolId : toolIds) {
            this.toolIds.add(Identifier.parse(toolId));
        }
    }

    @Override
    protected @Nullable Entry entryFor(final List<Ingredient> ingredients, final ItemStack result) {
        final List<ItemStack> tools = this.tools();
        if (tools.isEmpty()) {
            return null;
        }
        Ingredient input = null;
        int inputCount = 0;
        boolean hasTool = false;
        for (final Ingredient ingredient : ingredients) {
            if (!hasTool && acceptsAny(ingredient, tools)) {
                hasTool = true;
            } else if (input == null) {
                input = ingredient;
                inputCount = 1;
            } else if (input.equals(ingredient)) {
                inputCount++;
            } else {
                // A second, different ingredient
                return null;
            }
        }
        // The input being a tool itself (tool + tool) isn't a processing recipe
        if (!hasTool || input == null || acceptsAny(input, tools)) {
            return null;
        }
        return new Entry(input, inputCount, result);
    }

    private static boolean acceptsAny(final Ingredient ingredient, final List<ItemStack> stacks) {
        for (final ItemStack stack : stacks) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> tools() {
        final List<ItemStack> found = new ArrayList<>();
        for (final Identifier toolId : this.toolIds) {
            final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(toolId);
            if (item.isEmpty()) {
                return List.of();
            }
            found.add(new ItemStack(item.get()));
        }
        return found;
    }
}
