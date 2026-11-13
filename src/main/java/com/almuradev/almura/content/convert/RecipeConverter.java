/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 1.12 recipes ({@code crafting_shaped}, {@code crafting_shapeless}, {@code smelting}) in 26.3's format: ingredients as item ids (1.12's
 * {@code {"item", "data"}} flattened), results as {@code {"id", "count"}}.
 */
final class RecipeConverter {

    private RecipeConverter() {
    }

    /**
     * @return Why the recipe can't be converted (an unknown type, or an item 26.3 doesn't have), or null if it can
     */
    static @Nullable String problem(final JsonObject recipe, final Predicate<String> knownItem) {
        final String type = type(recipe);
        if (!type.equals("crafting_shaped") && !type.equals("crafting_shapeless") && !type.equals("smelting") && !type.equals("furnace")) {
            return "type " + type;
        }
        final List<JsonElement> stacks = new ArrayList<>();
        if (recipe.get("key") instanceof final JsonObject key) {
            key.entrySet().stream().map(Map.Entry::getValue).forEach(stacks::add);
        }
        if (recipe.get("ingredients") instanceof final JsonArray ingredients) {
            ingredients.forEach(stacks::add);
        }
        if (recipe.has("ingredient")) {
            stacks.add(recipe.get("ingredient"));
        }
        // Almura's 1.12 smelting format
        if (recipe.has("input")) {
            stacks.add(recipe.get("input"));
        }
        stacks.add(recipe.has("output") ? recipe.get("output") : recipe.get("result"));
        for (final JsonElement stack : stacks) {
            for (final JsonElement option : options(stack)) {
                final String id = item(option);
                if (id == null) {
                    return "an item it can't name: " + option;
                }
                if (!id.startsWith("#") && !knownItem.test(id)) {
                    return "unknown item " + id;
                }
            }
        }
        return null;
    }

    static @Nullable JsonObject convert(final JsonObject recipe) {
        final String type = type(recipe);
        final JsonObject converted = new JsonObject();
        converted.addProperty("type", "minecraft:" + type);
        if (recipe.has("group")) {
            converted.add("group", recipe.get("group"));
        }
        switch (type) {
            case "crafting_shaped" -> {
                converted.add("pattern", recipe.get("pattern"));
                final JsonObject key = new JsonObject();
                recipe.getAsJsonObject("key").entrySet().forEach(entry -> key.add(entry.getKey(), ingredient(entry.getValue())));
                converted.add("key", key);
                converted.add("result", result(recipe.get("result")));
            }
            case "crafting_shapeless" -> {
                final JsonArray ingredients = new JsonArray();
                for (final JsonElement ingredient : recipe.getAsJsonArray("ingredients")) {
                    // A count on an ingredient meant nothing to 1.12's reader (vanilla's): one of each
                    ingredients.add(ingredient(ingredient));
                }
                converted.add("ingredients", ingredients);
                converted.add("result", result(recipe.get("result")));
            }
            case "smelting", "furnace" -> {
                // Almura's 1.12 "furnace" recipes name them input and output
                converted.addProperty("type", "minecraft:smelting");
                converted.add("ingredient", ingredient(recipe.has("input") ? recipe.get("input") : recipe.get("ingredient")));
                converted.add("result", result(recipe.has("output") ? recipe.get("output") : recipe.get("result")));
                converted.addProperty("experience", recipe.has("experience") ? recipe.get("experience").getAsFloat() : 0.1f);
                converted.addProperty("cookingtime", recipe.has("cookingtime") ? recipe.get("cookingtime").getAsInt() : 200);
            }
            default -> {
                return null;
            }
        }
        return converted;
    }

    private static String type(final JsonObject recipe) {
        final String type = recipe.has("type") ? recipe.get("type").getAsString() : "";
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }

    /**
     * An ingredient: one item as its id, or a list of choices.
     */
    private static JsonElement ingredient(final JsonElement stack) {
        final List<JsonElement> options = options(stack);
        if (options.size() == 1) {
            return new com.google.gson.JsonPrimitive(item(options.getFirst()));
        }
        final JsonArray array = new JsonArray();
        options.forEach(option -> array.add(item(option)));
        return array;
    }

    private static JsonObject result(final JsonElement stack) {
        final JsonObject object = stack.getAsJsonObject();
        final JsonObject result = new JsonObject();
        result.addProperty("id", item(object));
        final int count = object.has("count") ? object.get("count").getAsInt() : 1;
        if (count != 1) {
            result.addProperty("count", count);
        }
        return result;
    }

    private static List<JsonElement> options(final @Nullable JsonElement stack) {
        if (stack == null) {
            return List.of();
        }
        if (stack instanceof final JsonArray array) {
            final List<JsonElement> options = new ArrayList<>();
            array.forEach(options::add);
            return options;
        }
        return List.of(stack);
    }

    /**
     * The 26.3 id of a 1.12 stack ({@code {"item": "minecraft:dye", "data": 15}} is {@code minecraft:bone_meal}), or null if it can't say.
     */
    private static @Nullable String item(final JsonElement stack) {
        if (stack.isJsonPrimitive()) {
            return Legacy.item(stack.getAsString(), 0);
        }
        final JsonObject object = stack.getAsJsonObject();
        if (!object.has("item")) {
            return null;
        }
        final int data = object.has("data") ? object.get("data").getAsInt() : 0;
        return Legacy.item(object.get("item").getAsString(), data);
    }
}
