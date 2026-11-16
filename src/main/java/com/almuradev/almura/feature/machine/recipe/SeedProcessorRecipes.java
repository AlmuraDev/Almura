/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * What the seed processor makes, as 3.5's: every crafting recipe (Almura content, vanilla or other mods) that turns a single ingredient into
 * seeds, an item whose id has "seed" in it. The machine gives what a crafting table would.
 */
public final class SeedProcessorRecipes extends ProcessingRecipes {

    @Override
    protected @Nullable Entry entryFor(final List<Ingredient> ingredients, final ItemStack result) {
        if (ingredients.size() != 1 || !BuiltInRegistries.ITEM.getKey(result.getItem()).getPath().toLowerCase(Locale.ROOT).contains("seed")) {
            return null;
        }
        return new Entry(ingredients.getFirst(), 1, result);
    }
}
