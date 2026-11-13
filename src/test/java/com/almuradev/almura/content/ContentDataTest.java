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
        final ContentPack pack = new ContentPackBuilder(source).buildData(parser.blocks(AlmuraContent.BLOCK_KINDS),
            id -> BuiltInRegistries.ITEM.containsKey(Identifier.parse(id)));
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        final List<String> failures = new ArrayList<>();
        int recipes = 0, tables = 0;
        for (final String path : pack.paths()) {
            final JsonElement json = pack.json(path);
            if (path.contains("/recipe/")) {
                recipes++;
                // Tags load with a world's data, so this test has none; a recipe naming one ("#c:dyes") is right in the game
                Recipe.DIRECT_CODEC.parse(ops, json).ifError(error -> {
                    if (!error.message().contains("Missing tag")) {
                        failures.add(path + ": " + error.message());
                    }
                });
            } else if (path.contains("/loot_table/")) {
                tables++;
                LootTable.DIRECT_CODEC.parse(ops, json).ifError(error -> failures.add(path + ": " + error.message()));
            }
        }
        System.out.println("Read " + recipes + " recipes and " + tables + " loot tables");
        failures.stream().limit(20).forEach(System.out::println);
        assertEquals(0, failures.size(), "recipes and loot tables 26.3 can't read");
    }
}
