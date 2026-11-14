/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.almuradev.almura.content.convert.ContentPack;
import com.almuradev.almura.content.convert.ContentPackBuilder;
import com.almuradev.almura.content.def.ContentParser;
import com.almuradev.almura.content.source.ContentSource;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootTable;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * With the game's mod loading started, every recipe and loot table the content pack generates reads with 26.3's own codecs.
 */
final class ContentDataTest {

    @Test
    void recipesAndLootTablesRead() {
        final String root = System.getProperty("almura.content");
        assumeTrue(root != null && Files.isDirectory(Path.of(root)), "The content isn't in the dev run");
        final ContentSource source = ContentSource.at(Path.of(root));
        final ContentParser parser = new ContentParser(source);
        final ContentPack pack = new ContentPackBuilder(source).crops(parser.crops(parser.seeds())).buildData(parser.blocks(AlmuraContent.BLOCK_KINDS),
            id -> BuiltInRegistries.ITEM.containsKey(Identifier.parse(id)));
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        final List<String> failures = new ArrayList<>();
        int recipes = 0, tables = 0;
        for (final String path : pack.paths()) {
            final JsonElement json = withoutNamedPredicates(pack.json(path));
            if (path.contains("/recipe/")) {
                recipes++;
                // Tags load with a world's data, so this test has none; a recipe naming one ("#c:dyes") is right in the game
                Recipe.DIRECT_CODEC.parse(ops, json).ifError(error -> {
                    if (!error.message().contains("Missing tag")) {
                        failures.add(path + ": " + error.message());
                    }
                }).ifSuccess(recipe -> Recipe.DIRECT_CODEC.encodeStart(ops, recipe)
                    .ifSuccess(written -> lost(json, written, path, failures)));
            } else if (path.contains("/loot_table/")) {
                tables++;
                LootTable.DIRECT_CODEC.parse(ops, json).ifError(error -> failures.add(path + ": " + error.message()))
                    .ifSuccess(table -> LootTable.DIRECT_CODEC.encodeStart(ops, table).ifSuccess(written -> lost(json, written, path, failures)));
            }
        }
        // The grass seed loot modifier (a resource of the mod, not of the pack)
        try (var in = ContentDataTest.class.getResourceAsStream("/data/almura/loot_modifiers/grass_seeds.json")) {
            final JsonElement modifier = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            net.neoforged.neoforge.common.loot.IGlobalLootModifier.DIRECT_CODEC.parse(ops, modifier)
                .ifError(error -> failures.add("loot_modifiers/grass_seeds.json: " + error.message()))
                .ifSuccess(read -> net.neoforged.neoforge.common.loot.IGlobalLootModifier.DIRECT_CODEC.encodeStart(ops, read)
                    .ifSuccess(written -> lost(modifier, written, "loot_modifiers/grass_seeds.json", failures)));
        } catch (final java.io.IOException e) {
            failures.add("loot_modifiers/grass_seeds.json: " + e);
        }
        System.out.println("Read " + recipes + " recipes and " + tables + " loot tables");
        failures.stream().limit(60).forEach(System.out::println);
        assertEquals(0, failures.size(), "recipes and loot tables 26.3 can't read, or reads without some of what they say");
    }

    /**
     * 26.3 ignores fields it doesn't know (1.20's "conditions" and "functions" in a loot table read as nothing, so every drop dropped
     * always): what 26.3 writes back from what it read must still hold everything that was given.
     */
    /**
     * Named predicates ({@code minecraft:tool/can_shear}, as vanilla's leaves use) are world data, which this test hasn't: each stands in
     * as a condition it has, so the rest of the table is still checked.
     */
    private static JsonElement withoutNamedPredicates(final JsonElement json) {
        if (json == null) {
            return null;
        }
        final JsonElement copy = json.deepCopy();
        replaceNamed(copy);
        return copy;
    }

    private static void replaceNamed(final JsonElement json) {
        if (json instanceof final com.google.gson.JsonObject object) {
            for (final String key : java.util.Set.copyOf(object.keySet())) {
                final JsonElement value = object.get(key);
                if ((key.equals("term") || key.equals("condition")) && value.isJsonPrimitive() && value.getAsString().startsWith("minecraft:tool/")) {
                    object.add(key, standIn());
                } else {
                    replaceNamed(value);
                }
            }
        } else if (json instanceof final com.google.gson.JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonPrimitive() && array.get(i).getAsString().startsWith("minecraft:tool/")) {
                    array.set(i, standIn());
                } else {
                    replaceNamed(array.get(i));
                }
            }
        }
    }

    private static com.google.gson.JsonObject standIn() {
        final com.google.gson.JsonObject condition = new com.google.gson.JsonObject();
        condition.addProperty("type", "minecraft:survives_explosion");
        return condition;
    }

    private static boolean isDefault(final JsonElement value) {
        if (value.isJsonPrimitive()) {
            final var primitive = value.getAsJsonPrimitive();
            return primitive.isNumber() && primitive.getAsDouble() == 0 || primitive.isBoolean() && !primitive.getAsBoolean();
        }
        return value.isJsonArray() && value.getAsJsonArray().isEmpty() || value.isJsonObject() && value.getAsJsonObject().isEmpty();
    }

    private static void lost(final JsonElement given, final JsonElement written, final String where, final List<String> failures) {
        if (given instanceof final com.google.gson.JsonObject object) {
            if (!(written instanceof final com.google.gson.JsonObject writtenObject)) {
                failures.add(where + ": read as " + written);
                return;
            }
            for (final var entry : object.entrySet()) {
                final JsonElement back = writtenObject.get(entry.getKey());
                if (back == null) {
                    // A default (no experience, an empty list) isn't written back
                    if (!isDefault(entry.getValue())) {
                        failures.add(where + "." + entry.getKey() + ": ignored by 26.3");
                    }
                } else {
                    lost(entry.getValue(), back, where + "." + entry.getKey(), failures);
                }
            }
        } else if (given instanceof final com.google.gson.JsonArray array) {
            if (!(written instanceof final com.google.gson.JsonArray writtenArray) || writtenArray.size() != array.size()) {
                // A single condition may be written back unwrapped, and a list of one as the one
                if (array.size() == 1) {
                    lost(array.get(0), written, where, failures);
                } else {
                    failures.add(where + ": read as " + written);
                }
                return;
            }
            for (int i = 0; i < array.size(); i++) {
                lost(array.get(i), writtenArray.get(i), where + "[" + i + "]", failures);
            }
        }
    }
}
