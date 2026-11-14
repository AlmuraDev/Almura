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
import com.almuradev.almura.content.def.ContentDefs.CropDef;
import com.almuradev.almura.content.def.ContentDefs.LeafDef;
import com.almuradev.almura.content.def.ContentDefs.SaplingDef;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import com.almuradev.almura.content.def.ContentDefs.SeedDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.def.ContentParser;
import com.almuradev.almura.content.source.ContentSource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
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

    private static final Set<String> ITEM_KINDS = AlmuraContent.ITEM_KINDS;
    private static final Set<String> BLOCK_KINDS = AlmuraContent.BLOCK_KINDS;

    private static ContentSource source;
    private static List<ItemGroupDef> groups;
    private static List<ItemDef> items;
    private static List<BlockDef> blocks;
    private static List<SeedDef> seeds;
    private static List<CropDef> crops;
    private static ContentPack pack;
    private static ContentPackBuilder builder;
    private static Set<String> vanillaItems, vanillaTextures, vanillaModels, vanillaBlocks, vanillaBiomes;

    @BeforeAll
    static void build() throws IOException {
        final String root = System.getProperty("almura.test.content");
        assumeTrue(root != null && Files.isDirectory(Path.of(root)), "The content isn't in the dev run");
        source = ContentSource.at(Path.of(root));
        final ContentParser parser = new ContentParser(source);
        groups = parser.itemGroups();
        items = parser.items(ITEM_KINDS);
        blocks = parser.blocks(BLOCK_KINDS);
        seeds = parser.seeds();
        crops = parser.crops(seeds);
        readVanilla();
        final Set<String> known = new HashSet<>(vanillaItems);
        items.forEach(item -> known.add(item.id()));
        blocks.forEach(block -> known.add(block.id()));
        // Almura's own items the content uses (the cruets)
        known.addAll(ContentPackBuilder.OWN_ITEMS_WITH_CONTENT_MODELS);
        builder = new ContentPackBuilder(source).crops(crops)
            .generation(parser.treeGeneration(), parser.grassGeneration(), parser.oreGeneration());
        pack = builder.build(groups, items, blocks, known::contains);
    }

    /**
     * Vanilla's item ids, textures and models, from the game jar the build uses.
     */
    private static void readVanilla() throws IOException {
        vanillaItems = new HashSet<>();
        vanillaTextures = new HashSet<>();
        vanillaModels = new HashSet<>();
        vanillaBlocks = new HashSet<>();
        vanillaBiomes = new HashSet<>();
        final Path artifacts = Path.of(System.getProperty("almura.test.artifacts", "build/moddev/artifacts"));
        assumeTrue(Files.isDirectory(artifacts), "The game jar isn't built");
        try (Stream<Path> files = Files.list(artifacts)) {
            final Path jar = files.filter(path -> path.getFileName().toString().endsWith("-merged.jar")).findFirst().orElse(null);
            assumeTrue(jar != null, "The game jar isn't built");
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                zip.stream().map(ZipEntry::getName).forEach(name -> {
                    if (name.startsWith("assets/minecraft/blockstates/") && name.endsWith(".json")) {
                        vanillaBlocks.add("minecraft:" + strip(name, "assets/minecraft/blockstates/", ".json"));
                    } else if (name.startsWith("data/minecraft/worldgen/biome/") && name.endsWith(".json")) {
                        vanillaBiomes.add("minecraft:" + strip(name, "data/minecraft/worldgen/biome/", ".json"));
                    }
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
        assertEquals(186 + 413 + 110 + 5 + 5 + 85, items.size(), "normal, food, slab, hoe, sickle and seed items");
        assertEquals(3 + 764 + 22 + 99 + 128 + 220 + 85 + 42 + 25 + 69, blocks.size(),
            "normal, horizontal, log, flower, stair, slab (single and double), crop, leaf, sapling and container blocks");
        assertEquals(85, seeds.size(), "seeds");
        assertEquals(85, crops.size(), "crops");
    }

    @Test
    void worldGenerationIsWhole() {
        final ContentParser parser = new ContentParser(source);
        final var trees = parser.treeGeneration();
        final var grasses = parser.grassGeneration();
        final var mixes = parser.grasses();
        final var ores = parser.oreGeneration();
        assertEquals(25, trees.size(), "tree generation");
        assertEquals(1, grasses.size(), "grass generation");
        assertEquals(1, ores.size(), "ore generation");
        final Set<String> blockIds = new HashSet<>(vanillaBlocks);
        blocks.forEach(block -> blockIds.add(block.id()));
        final Set<String> treeIds = new HashSet<>();
        parser.trees().forEach(tree -> treeIds.add(tree.id()));
        final List<String> wrong = new ArrayList<>();
        trees.forEach(tree -> {
            if (!treeIds.contains(tree.tree())) {
                wrong.add(tree.id() + ": grows " + tree.tree() + ", which isn't a tree");
            }
            tree.chance().forEach(range -> range.biomes().stream().filter(biome -> biome.contains(":") && !vanillaBiomes.contains(biome))
                .forEach(biome -> wrong.add(tree.id() + ": biome " + biome + " isn't 26.3's")));
            tree.requires().stream().filter(block -> !blockIds.contains(block)).forEach(block -> wrong.add(tree.id() + ": requires " + block));
        });
        mixes.forEach(mix -> mix.entries().forEach(entry -> entry.blocks().stream().filter(state -> !blockIds.contains(state.block()))
            .forEach(state -> wrong.add(mix.id() + ": " + state.block() + " isn't a block"))));
        ores.forEach(ore -> ore.ores().stream().filter(entry -> !blockIds.contains(entry.block()))
            .forEach(entry -> wrong.add("ore " + entry.block() + " isn't a block")));
        report("world generation not whole", wrong);
        assertEquals(List.of(), wrong);
        assertTrue(pack.has("data/almura/neoforge/biome_modifier/content_vegetation.json"), "trees and grass in the overworld");
        assertTrue(pack.has("data/almura/neoforge/biome_modifier/content_ores.json"), "ores in the overworld");
    }

    @Test
    void everyTreeIsWhole() {
        final ContentParser parser = new ContentParser(source);
        final List<LeafDef> leaves = parser.leaves();
        final List<SaplingDef> saplings = parser.saplings();
        final List<TreeDef> trees = parser.trees();
        assertEquals(42, leaves.size(), "leaves");
        assertEquals(25, saplings.size(), "saplings");
        assertEquals(25, trees.size(), "trees");
        final Set<String> blockIds = new HashSet<>();
        blocks.forEach(block -> blockIds.add(block.id()));
        final Set<String> treeIds = new HashSet<>();
        trees.forEach(tree -> treeIds.add(tree.id()));
        final List<String> wrong = new ArrayList<>();
        for (final SaplingDef sapling : saplings) {
            if (!treeIds.contains(sapling.tree())) {
                wrong.add(sapling.id() + ": grows " + sapling.tree() + ", which isn't a tree");
            }
        }
        for (final TreeDef tree : trees) {
            for (final String block : new String[] {tree.log(), tree.leaves(), tree.fruit()}) {
                if (block != null && !blockIds.contains(block)) {
                    wrong.add(tree.id() + ": is made of " + block + ", which isn't a block");
                }
            }
        }
        for (final LeafDef leaf : leaves) {
            if (leaf.spreadBlock() != null && !blockIds.contains(leaf.spreadBlock())) {
                wrong.add(leaf.id() + ": spreads " + leaf.spreadBlock() + ", which isn't a block");
            }
        }
        report("trees not whole", wrong);
        assertEquals(List.of(), wrong);
    }

    @Test
    void everyCropHasItsStagesSeedAndLoot() {
        final List<String> wrong = new ArrayList<>();
        final Set<String> seedIds = new HashSet<>();
        seeds.forEach(seed -> seedIds.add(seed.id()));
        for (final CropDef crop : crops) {
            if (!seedIds.contains(crop.seed())) {
                wrong.add(crop.id() + ": seed " + crop.seed() + " isn't a seed");
            }
            final JsonElement blockstate = pack.json("assets/" + ns(crop.id()) + "/blockstates/" + p(crop.id()) + ".json");
            for (int age = 0; age <= crop.maxAge(); age++) {
                if (!(blockstate instanceof final JsonObject object) || !object.getAsJsonObject("variants").has("age=" + age)) {
                    wrong.add(crop.id() + ": no model for age " + age);
                }
                if (crop.stages().get(age).growthChance() == null && age < crop.maxAge()) {
                    wrong.add(crop.id() + ": doesn't grow at age " + age);
                }
            }
            if (!pack.has("data/" + ns(crop.id()) + "/loot_table/blocks/" + p(crop.id()) + ".json")) {
                wrong.add(crop.id() + ": no loot table");
            }
            if (crop.stages().getLast().state().drops().isEmpty()) {
                wrong.add(crop.id() + ": drops nothing when ripe");
            }
        }
        for (final SeedDef seed : seeds) {
            if (crops.stream().noneMatch(crop -> crop.id().equals(seed.crop()))) {
                wrong.add(seed.id() + ": plants " + seed.crop() + ", which isn't a crop");
            }
        }
        report("crops and seeds not right", wrong);
        assertEquals(List.of(), wrong);
    }

    @Test
    void everyBlockHasABlockstateAndAnItemModel() {
        final List<String> missing = new ArrayList<>();
        for (final BlockDef block : blocks) {
            if (block.isDoubleSlab()) {
                continue;
            }
            final String path = block.id().replace(":", "/");
            if (!pack.has("assets/" + ns(block.id()) + "/blockstates/" + p(block.id()) + ".json")) {
                missing.add("blockstate " + block.id());
            }
            // A crop's item is its seed
            if (!block.kind().equals("crop") && !pack.has("assets/" + ns(block.id()) + "/items/" + p(block.id()) + ".json")) {
                missing.add("item model " + path);
            }
        }
        for (final ItemDef item : items) {
            if (!pack.has("assets/" + ns(item.id()) + "/items/" + p(item.id()) + ".json")) {
                missing.add("item model " + item.id());
            }
        }
        report("missing", missing);
        assertEquals(List.of(), missing.stream().filter(line -> !AWAITING_DECISION.stream().anyMatch(line::contains)).toList(),
            "blocks or items without models");
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
        assertEquals(List.of(), missing, "missing models or textures");
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
            // NeoForge reads the material library the OBJ names (mtllib almura:models/...); without it the OBJ doesn't load at all
            final Path objFile = pack.file("assets/" + ns(obj) + "/" + p(obj));
            if (objFile != null) {
                try {
                    final String mtl = Files.readAllLines(objFile).stream().map(String::trim).filter(line -> line.startsWith("mtllib "))
                        .map(line -> line.substring("mtllib ".length()).trim()).findFirst().orElse(null);
                    if (mtl == null || !pack.has("assets/" + ns(mtl) + "/" + p(mtl))) {
                        missing.add("material library " + mtl + " for " + model);
                    }
                } catch (final IOException e) {
                    missing.add("OBJ " + obj + " unreadable: " + e.getMessage());
                }
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
            // A slab's item is its block's, named with it
            if (!item.kind().equals("slab") && !lang.has("item." + ns(item.id()) + "." + p(item.id()).replace('/', '.'))) {
                missing.add("item " + item.id());
            }
        }
        for (final BlockDef block : blocks) {
            if (!block.isDoubleSlab() && !lang.has("block." + ns(block.id()) + "." + p(block.id()).replace('/', '.'))) {
                missing.add("block " + block.id());
            }
        }
        for (final ItemGroupDef group : groups) {
            if (!lang.has(ContentPackBuilder.itemGroupKey(group.id()))) {
                missing.add("item group " + group.id());
            }
        }
        report("without a name", missing);
        assertEquals(List.of(), missing, "without a name");
    }

    @Test
    void everyPathIsAValidIdentifier() {
        // One invalid name (capitals, spaces) makes 26.3 throw out every resource pack on reload
        final List<String> invalid = new ArrayList<>();
        for (final String path : pack.paths()) {
            final String[] parts = path.split("/", 3);
            if (parts.length < 3 || !Identifier.isValidNamespace(parts[1]) || !Identifier.isValidPath(parts[2])) {
                invalid.add(path);
            }
        }
        report("not valid 26.3 resource names", invalid);
        assertTrue(invalid.isEmpty(), "Invalid resource names: " + invalid);
    }

    @Test
    void everyObjParticleIsCut() throws IOException {
        // Each OBJ's particle is cut from its texture sheet when first read; one that can't be is served as nothing
        final List<String> failed = new ArrayList<>();
        int cut = 0;
        for (final String path : pack.under("assets/almura/textures/block/obj_particle/").keySet()) {
            try (var in = pack.open(path)) {
                if (in == null || in.readAllBytes().length == 0) {
                    failed.add(path);
                } else {
                    cut++;
                }
            }
        }
        report("OBJ particles not cut", failed);
        final long objModels = pack.under("assets/almura/models/obj/").size();
        report("OBJ particles cut", List.of(cut + " of " + objModels + " OBJ models"));
        assertTrue(cut >= objModels * 9 / 10, "Only " + cut + " OBJ particles for " + objModels + " OBJ models");
        assertTrue(failed.isEmpty(), "OBJ particles not cut: " + failed);
    }

    /**
     * What's known to be wrong in the 4.0 content and waits on someone to decide (tools/content-4.0's FIXES.md), and the items of 3.5
     * features 4.0 hasn't yet: the only problems the content may have.
     */
    private static final List<String> AWAITING_DECISION = List.of(
        "grief_prevention_inspector", "grief_prevention_selector", "world_edit_selector",
        "texture almura:block/letter/seedbarrels");
    private static final List<String> NOT_YET_PORTED = List.of(
        "unknown item almura:normal/tool/skills_handbook",
        "unknown item almura:normal/tool/harvesting_sack", "unknown item almura:normal/tool/farmers_almanac", "unknown item mocreatures:");

    /**
     * The 4.0 content is right as it is: the loader repairs nothing, so anything it had to leave out is a mistake in the content (fix it in
     * tools/content-4.0/fix_content.py, or in the content).
     */
    @Test
    void theContentNeedsNoRepair() {
        final List<String> wrong = builder.problems().stream()
            .filter(problem -> AWAITING_DECISION.stream().noneMatch(problem::contains) && NOT_YET_PORTED.stream().noneMatch(problem::contains))
            .toList();
        report("mistakes in the content", wrong);
        assertEquals(List.of(), wrong);
    }

    /**
     * Block models as 26.3 draws them: every element's corners the right way round (26.3 turns the faces of one given backwards inward),
     * and a particle texture for one that draws its own elements.
     */
    @Test
    void blockModelsAreWhole() {
        final List<String> wrong = new ArrayList<>();
        for (final String path : pack.paths()) {
            if (!path.startsWith("assets/almura/models/block/") || !path.endsWith(".json")) {
                continue;
            }
            final JsonObject model = pack.json(path).getAsJsonObject();
            if (model.get("elements") instanceof final com.google.gson.JsonArray elements && !elements.isEmpty()) {
                if (!(model.get("textures") instanceof final JsonObject textures) || !textures.has("particle")) {
                    wrong.add(path + ": no particle texture");
                }
                for (final JsonElement element : elements) {
                    final var from = element.getAsJsonObject().getAsJsonArray("from");
                    final var to = element.getAsJsonObject().getAsJsonArray("to");
                    for (int axis = 0; axis < 3; axis++) {
                        if (from.get(axis).getAsDouble() > to.get(axis).getAsDouble()) {
                            wrong.add(path + ": an element's corners are the wrong way round");
                            break;
                        }
                    }
                }
            }
        }
        report("block models not whole", wrong);
        assertEquals(List.of(), wrong);
    }

    /**
     * The OBJs as NeoForge reads them: each names its material library by its full path, which names its textures by id.
     */
    @Test
    void objsAreAsNeoForgeReadsThem() throws IOException {
        final List<String> wrong = new ArrayList<>();
        final Path models = source.namespace("almura").resolve("models");
        try (Stream<Path> files = Files.walk(models)) {
            for (final Path obj : files.filter(path -> path.toString().endsWith(".obj")).toList()) {
                final String mtllib = Files.readAllLines(obj).stream().map(String::trim).filter(line -> line.startsWith("mtllib ")).findFirst()
                    .orElse(null);
                if (mtllib == null || !mtllib.startsWith("mtllib almura:models/")) {
                    wrong.add(models.relativize(obj) + ": " + (mtllib == null ? "no mtllib" : mtllib));
                }
            }
        }
        try (Stream<Path> files = Files.walk(models)) {
            for (final Path mtl : files.filter(path -> path.toString().endsWith(".mtl")).toList()) {
                for (final String line : Files.readAllLines(mtl)) {
                    final String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2 && parts[0].startsWith("map_") && (!parts[1].contains(":") || parts[1].endsWith(".png"))) {
                        wrong.add(models.relativize(mtl) + ": " + line.trim());
                    }
                }
            }
        }
        report("OBJs not as NeoForge reads them", wrong);
        // The tv's texture isn't in the content at all (awaiting a decision)
        assertEquals(List.of(), wrong.stream().filter(line -> !line.contains("letter/seedbarrels")).toList());
    }

    /**
     * Wild crops in the grass mixes are ripe (only a ripe crop stands on grass), and every potion effect named is a 26.3 id.
     */
    @Test
    void wildCropsAndEffectsAreRight() {
        final ContentParser parser = new ContentParser(source);
        final java.util.Map<String, Integer> ripe = new java.util.HashMap<>();
        crops.forEach(crop -> ripe.put(crop.id(), crop.maxAge()));
        final List<String> wrong = new ArrayList<>();
        parser.grasses().forEach(mix -> mix.entries().forEach(entry -> entry.blocks().forEach(state -> {
            if (ripe.containsKey(state.block()) && !String.valueOf(ripe.get(state.block())).equals(state.properties().get("age"))) {
                wrong.add(mix.id() + ": " + state.block() + " at age " + state.properties().get("age") + ", ripe is " + ripe.get(state.block()));
            }
        })));
        items.stream().filter(item -> item.food() != null).forEach(item -> item.food().effects().forEach(effect -> effect.effects().forEach(id -> {
            if (net.minecraft.resources.Identifier.tryParse(id) == null) {
                wrong.add(item.id() + ": effect " + id + " isn't an id");
            }
        })));
        report("wild crops and effects not right", wrong);
        assertEquals(List.of(), wrong);
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
        Files.writeString(Path.of(System.getProperty("almura.test.build", "build")).resolve("content-report.txt"), text, StandardCharsets.UTF_8);
        // A few generated files to read, one of each kind
        final Path samples = Path.of(System.getProperty("almura.test.build", "build")).resolve("content-samples");
        Files.createDirectories(samples);
        for (final String prefix : List.of("assets/almura/blockstates/horizontal/building/afledos_plank", "assets/almura/blockstates/container",
            "assets/almura/models/obj/block/", "assets/almura/items/food/food/pizza_deluxe", "assets/almura/items/horizontal/building/afledos_plank",
            "data/almura/recipe/normal/ingredient/barbecuesauce", "data/almura/recipe/horizontal/building/bricks_block_b_supersmooth",
            "data/almura/loot_table/blocks/horizontal/ore/marbleore", "data/almura/loot_table/blocks/crop/blackberry", "data/minecraft/tags/block/mineable/pickaxe",
            "data/minecraft/tags/block/needs_stone_tool")) {
            pack.paths().stream().filter(path -> path.startsWith(prefix) && path.endsWith(".json")).findFirst().ifPresent(path -> {
                try (var in = pack.open(path)) {
                    Files.write(samples.resolve(path.replace('/', '_')), in.readAllBytes());
                } catch (final IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
        for (final String path : pack.under("assets/almura/textures/block/obj_particle/").keySet()) {
            if (path.contains("cep_innkeeper_bar")) {
                try (var in = pack.open(path)) {
                    if (in != null) {
                        Files.write(samples.resolve(path.replace('/', '_')), in.readAllBytes());
                    }
                }
            }
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
