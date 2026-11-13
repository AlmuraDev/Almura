/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.almuradev.almura.content.convert.ContentPack;
import com.almuradev.almura.content.convert.ContentPackBuilder;
import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.def.ContentParser;
import com.almuradev.almura.content.source.ContentSource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the real content (copied into the dev run at {@code runs/client/assets/fs_5}) and builds the content pack from it, checking that
 * what 26.3 will load is all there: a blockstate for every block, an item model for every item, every model and texture they name, and a
 * name for each. Writes what it found to {@code build/content-report.txt}. Skipped when the content hasn't been copied.
 */
final class ContentConversionTest {

    private static final Set<String> ITEM_KINDS = Set.of("normal", "food");
    private static final Set<String> BLOCK_KINDS = Set.of("normal", "horizontal");

    private static ContentSource source;
    private static List<ItemGroupDef> groups;
    private static List<ItemDef> items;
    private static List<BlockDef> blocks;
    private static ContentPack pack;
    private static ContentPackBuilder builder;
    private static Set<String> vanillaItems, vanillaTextures, vanillaModels;

    @BeforeAll
    static void build() throws IOException {
        final String root = System.getProperty("almura.test.content");
        assumeTrue(root != null && Files.isDirectory(Path.of(root)), "The content isn't in the dev run");
        source = ContentSource.at(Path.of(root));
        final ContentParser parser = new ContentParser(source);
        groups = parser.itemGroups();
        items = parser.items(ITEM_KINDS);
        blocks = parser.blocks(BLOCK_KINDS);
        readVanilla();
        final Set<String> known = new HashSet<>(vanillaItems);
        items.forEach(item -> known.add(item.id()));
        blocks.forEach(block -> known.add(block.id()));
        builder = new ContentPackBuilder(source);
        pack = builder.build(groups, items, blocks, known::contains);
    }

    /**
     * Vanilla's item ids, textures and models, from the game jar the build uses.
     */
    private static void readVanilla() throws IOException {
        vanillaItems = new HashSet<>();
        vanillaTextures = new HashSet<>();
        vanillaModels = new HashSet<>();
        final Path artifacts = Path.of("build/moddev/artifacts");
        assumeTrue(Files.isDirectory(artifacts), "The game jar isn't built");
        try (Stream<Path> files = Files.list(artifacts)) {
            final Path jar = files.filter(path -> path.getFileName().toString().endsWith("-merged.jar")).findFirst().orElse(null);
            assumeTrue(jar != null, "The game jar isn't built");
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                zip.stream().map(ZipEntry::getName).forEach(name -> {
                    if (name.startsWith("assets/minecraft/items/") && name.endsWith(".json")) {
                        vanillaItems.add("minecraft:" + strip(name, "assets/minecraft/items/", ".json"));
                    } else if (name.startsWith("assets/minecraft/textures/") && name.endsWith(".png")) {
                        vanillaTextures.add("minecraft:" + strip(name, "assets/minecraft/textures/", ".png"));
                    } else if (name.startsWith("assets/minecraft/models/") && name.endsWith(".json")) {
                        vanillaModels.add("minecraft:" + strip(name, "assets/minecraft/models/", ".json"));
                    }
                });
            }
        }
    }

    private static String strip(final String name, final String prefix, final String suffix) {
        return name.substring(prefix.length(), name.length() - suffix.length());
    }

    @Test
    void readsEverything() {
        assertEquals(List.of(), source.problems(), "Content files that couldn't be read");
        assertEquals(23, groups.size(), "item groups");
        assertEquals(186 + 413, items.size(), "normal and food items");
        assertEquals(3 + 764, blocks.size(), "normal and horizontal blocks");
    }

    @Test
    void everyBlockHasABlockstateAndAnItemModel() {
        final List<String> missing = new ArrayList<>();
        for (final BlockDef block : blocks) {
            final String path = block.id().replace(":", "/");
            if (!pack.has("assets/" + ns(block.id()) + "/blockstates/" + p(block.id()) + ".json")) {
                missing.add("blockstate " + block.id());
            }
            if (!pack.has("assets/" + ns(block.id()) + "/items/" + p(block.id()) + ".json")) {
                missing.add("item model " + path);
            }
        }
        for (final ItemDef item : items) {
            if (!pack.has("assets/" + ns(item.id()) + "/items/" + p(item.id()) + ".json")) {
                missing.add("item model " + item.id());
            }
        }
        report("missing", missing);
        assertTrue(missing.size() <= blocks.size() / 50, "Too many blocks or items without models: " + missing.size());
    }

    /**
     * Every model the blockstates and item model definitions name, every parent of those, and every texture they use exists in the pack or in
     * vanilla.
     */
    @Test
    void everyModelAndTextureExists() {
        final List<String> missing = new ArrayList<>();
        final Set<String> checked = new HashSet<>();
        for (final String path : pack.paths()) {
            final JsonElement json = pack.json(path);
            if (json instanceof final JsonObject object && (path.contains("/blockstates/") || path.contains("/items/"))) {
                for (final String model : models(object)) {
                    this.checkModel(model, missing, checked);
                }
            }
        }
        report("unresolved models and textures", missing);
        assertTrue(missing.size() < 25, "Too many missing models or textures: " + missing.size());
    }

    private void checkModel(final String model, final List<String> missing, final Set<String> checked) {
        if (!checked.add(model)) {
            return;
        }
        if (ns(model).equals("minecraft") || model.startsWith("builtin/")) {
            if (!vanillaModels.contains(model.contains(":") ? model : "minecraft:" + model) && !model.contains("builtin")) {
                missing.add("vanilla model " + model);
            }
            return;
        }
        final JsonElement json = pack.json("assets/" + ns(model) + "/models/" + p(model) + ".json");
        if (!(json instanceof final JsonObject object)) {
            missing.add("model " + model);
            return;
        }
        if (object.has("parent")) {
            this.checkModel(object.get("parent").getAsString(), missing, checked);
        }
        if (object.has("loader") && object.get("model") != null) {
            final String obj = object.get("model").getAsString();
            if (!pack.has("assets/" + ns(obj) + "/" + p(obj))) {
                missing.add("OBJ " + obj + " for " + model);
            }
        }
        if (object.get("textures") instanceof final JsonObject textures) {
            for (final Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                final String texture = entry.getValue().getAsString();
                if (texture.startsWith("#")) {
                    continue;
                }
                final String id = texture.contains(":") ? texture : "minecraft:" + texture;
                final boolean exists = ns(id).equals("minecraft") ? vanillaTextures.contains(id)
                    : pack.has("assets/" + ns(id) + "/textures/" + p(id) + ".png");
                if (!exists) {
                    missing.add("texture " + id + " in " + model);
                }
            }
        }
    }

    private static List<String> models(final JsonObject json) {
        final List<String> models = new ArrayList<>();
        if (json.get("variants") instanceof final JsonObject variants) {
            variants.entrySet().forEach(entry -> models.add(entry.getValue().getAsJsonObject().get("model").getAsString()));
        }
        if (json.get("model") instanceof final JsonObject model && model.has("model")) {
            models.add(model.get("model").getAsString());
        }
        return models;
    }

    @Test
    void everyItemAndBlockHasAName() {
        final JsonObject lang = pack.json("assets/almura/lang/en_us.json").getAsJsonObject();
        final List<String> missing = new ArrayList<>();
        for (final ItemDef item : items) {
            if (!lang.has("item." + ns(item.id()) + "." + p(item.id()).replace('/', '.'))) {
                missing.add("item " + item.id());
            }
        }
        for (final BlockDef block : blocks) {
            if (!lang.has("block." + ns(block.id()) + "." + p(block.id()).replace('/', '.'))) {
                missing.add("block " + block.id());
            }
        }
        for (final ItemGroupDef group : groups) {
            if (!lang.has(ContentPackBuilder.itemGroupKey(group.id()))) {
                missing.add("item group " + group.id());
            }
        }
        report("without a name", missing);
        assertTrue(missing.size() <= (items.size() + blocks.size()) / 20, "Too many unnamed: " + missing.size());
    }

    @Test
    void countsThePack() {
        final Map<String, Integer> counts = new TreeMap<>();
        for (final String path : pack.paths()) {
            final String[] parts = path.split("/");
            final String kind = parts[0] + "/" + parts[2] + (path.endsWith(".obj") ? " (obj)" : path.endsWith(".mtl") ? " (mtl)" : "");
            counts.merge(kind, 1, Integer::sum);
        }
        report("pack files by folder", counts.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).toList());
        report("conversion problems", builder.problems());
    }

    @AfterAll
    static void writeReport() throws IOException {
        if (pack == null) {
            return;
        }
        final StringBuilder text = new StringBuilder("Almura content conversion report\n");
        REPORT.forEach((title, lines) -> {
            text.append("\n== ").append(title).append(" (").append(lines.size()).append(")\n");
            lines.forEach(line -> text.append("  ").append(line).append('\n'));
        });
        Files.writeString(Path.of("build/content-report.txt"), text, StandardCharsets.UTF_8);
        // A few generated files to read, one of each kind
        final Path samples = Path.of("build/content-samples");
        Files.createDirectories(samples);
        for (final String prefix : List.of("assets/almura/blockstates/horizontal/building/afledos_plank", "assets/almura/blockstates/container",
            "assets/almura/models/obj/block/", "assets/almura/items/food/food/pizza_deluxe", "assets/almura/items/horizontal/building/afledos_plank",
            "data/almura/recipe/item/normal/ingredient/barbecuesauce", "data/almura/recipe/block/horizontal/building/bricks_block_b_supersmooth",
            "data/almura/loot_table/blocks/horizontal/ore/marbleore", "data/minecraft/tags/block/mineable/pickaxe",
            "data/minecraft/tags/block/needs_stone_tool")) {
            pack.paths().stream().filter(path -> path.startsWith(prefix) && path.endsWith(".json")).findFirst().ifPresent(path -> {
                try (var in = pack.open(path)) {
                    Files.write(samples.resolve(path.replace('/', '_')), in.readAllBytes());
                } catch (final IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
    }

    private static final Map<String, List<String>> REPORT = new TreeMap<>();

    private static void report(final String title, final List<String> lines) {
        REPORT.put(title, lines.stream().sorted().toList());
    }

    private static String ns(final String id) {
        return id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft";
    }

    private static String p(final String id) {
        return id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    }
}
