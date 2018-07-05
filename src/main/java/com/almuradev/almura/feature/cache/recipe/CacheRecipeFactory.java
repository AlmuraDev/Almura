/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class CacheRecipeFactory implements IRecipeFactory {

    /**
     * This constructor is called by forge via reflection.
     */
    public CacheRecipeFactory() {
    }

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        final String group = JsonUtils.getString(json, "group", "");

        final Map<Character, Ingredient> ingMap = new HashMap<>();
        for (final Map.Entry<String, JsonElement> entry: JsonUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            if (entry.getKey().charAt(0) == ' ')
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }
        ingMap.put(' ', Ingredient.EMPTY);

        final JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

        if (patternJ.size() == 0)
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        if (patternJ.size() > 3)
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");

        final String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; x++) {
            final String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (line.length() > 3)
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            if (x > 0 && pattern[0].length() != line.length())
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            pattern[x] = line;
        }

        final NonNullList<Ingredient> input = NonNullList.withSize(pattern[0].length() * pattern.length, Ingredient.EMPTY);
        final Set<Character> keys = new HashSet<>(ingMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (final String line: pattern) {
            for (final char chr: line.toCharArray()) {
                final Ingredient ing = ingMap.get(chr);
                if (ing == null)
                    throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                input.set(x++, ing);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty())
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

        final ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        return new CacheRecipe(group, pattern[0].length(), pattern.length, input, result);
    }
}
