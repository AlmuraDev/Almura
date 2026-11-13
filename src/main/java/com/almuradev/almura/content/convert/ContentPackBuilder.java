/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.convert;

import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.DropDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.source.ContentSource;
import com.almuradev.almura.content.source.ContentSource.ContentFile;
import com.almuradev.almura.content.source.ContentSource.TranslationFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Builds the pack that carries the content into 26.3 ({@link ContentPack}): the content folders' textures, OBJ models and material libraries
 * as they are; their JSON models with 1.12's texture names renamed; and, generated, 26.3 blockstates, OBJ wrapper models, item model
 * definitions, the language file, recipes, loot tables and tool tags for what was registered. No Minecraft classes, so it is tested against
 * the real content.
 */
public final class ContentPackBuilder {

    private static final String[] FACINGS = {"north", "east", "south", "west"};

    private final ContentSource source;
    private final ContentPack pack = new ContentPack();
    private final List<String> problems = new ArrayList<>();

    public ContentPackBuilder(final ContentSource source) {
        this.source = source;
    }

    /**
     * @param knownItem whether an item id exists, for recipes: one using an unknown item is left out
     */
    public ContentPack build(final List<ItemGroupDef> groups, final List<ItemDef> items, final List<BlockDef> blocks,
        final Predicate<String> knownItem) {
        for (final String namespace : this.source.namespaces()) {
            this.passThrough(namespace);
        }
        for (final BlockDef block : blocks) {
            this.blockstate(block);
            this.blockItemModel(block);
            this.lootTable(block);
        }
        for (final ItemDef item : items) {
            this.itemModel(item);
        }
        this.toolTags(blocks);
        this.language(groups);
        this.recipes(knownItem);
        return this.pack;
    }

    public List<String> problems() {
        return List.copyOf(this.problems);
    }

    /**
     * Textures, OBJ models and material libraries as they are; JSON models with 1.12's vanilla texture names renamed. The 1.12 blockstates
     * aren't 26.3's format, so only the converted ones (for what was registered) are served.
     */
    private void passThrough(final String namespace) {
        final Path folder = this.source.namespace(namespace);
        for (final String file : this.source.assetFiles(namespace)) {
            if (file.startsWith("blockstates/")) {
                continue;
            }
            final String path = "assets/" + namespace + "/" + file;
            if (file.startsWith("models/") && file.endsWith(".json")) {
                final JsonObject model = this.source.readObject(folder.resolve(file));
                if (model != null) {
                    this.pack.putJson(path, model(model));
                }
            } else {
                this.pack.putFile(path, folder.resolve(file));
            }
        }
    }

    /**
     * A 1.12 JSON model as 26.3 reads it: vanilla textures renamed ({@code blocks/dirt} as {@code block/dirt}).
     */
    static JsonObject model(final JsonObject model) {
        final JsonObject copy = model.deepCopy();
        if (copy.get("textures") instanceof final JsonObject textures) {
            for (final String key : Set.copyOf(textures.keySet())) {
                if (textures.get(key).isJsonPrimitive()) {
                    textures.addProperty(key, Legacy.texture(textures.get(key).getAsString()));
                }
            }
        }
        return copy;
    }

    /**
     * The block's 26.3 blockstate from its 1.12 Forge one: a variant per facing (horizontal blocks) or the single default variant.
     */
    private void blockstate(final BlockDef block) {
        final String namespace = namespace(block.id()), path = path(block.id());
        final Path file = this.source.namespace(namespace).resolve("blockstates").resolve(path + ".json");
        if (!Files.isRegularFile(file)) {
            this.problems.add(block.id() + ": no blockstate (blockstates/" + path + ".json)");
            return;
        }
        final JsonObject forge = this.source.readObject(file);
        if (forge == null) {
            return;
        }
        final JsonObject variants = new JsonObject();
        if (block.kind().equals("horizontal")) {
            for (final String facing : FACINGS) {
                final JsonObject variant = this.variant(block, forge, "facing", facing);
                if (variant != null) {
                    variants.add("facing=" + facing, variant);
                }
            }
        } else {
            final JsonObject variant = this.variant(block, forge, null, "normal");
            if (variant != null) {
                variants.add("", variant);
            }
        }
        final JsonObject blockstate = new JsonObject();
        blockstate.add("variants", variants);
        this.pack.putJson("assets/" + namespace + "/blockstates/" + path + ".json", blockstate);
    }

    /**
     * One variant: Forge's defaults, then the property value's settings ({@code "facing": {"south": {"y": 180}}}), then the full variant's
     * ({@code "facing=south"} or {@code "normal"}).
     */
    private @Nullable JsonObject variant(final BlockDef block, final JsonObject forge, final @Nullable String property, final String value) {
        final JsonObject settings = new JsonObject();
        merge(settings, forge.get("defaults"));
        final JsonObject variants = forge.get("variants") instanceof final JsonObject object ? object : new JsonObject();
        if (property != null && variants.get(property) instanceof final JsonObject values) {
            merge(settings, values.get(value));
        }
        merge(settings, variants.get(property == null ? value : property + "=" + value));
        if (!settings.has("model")) {
            this.problems.add(block.id() + ": the blockstate names no model");
            return null;
        }
        final String model = this.modelReference(block.id(), settings.get("model").getAsString());
        if (model == null) {
            return null;
        }
        final JsonObject variant = new JsonObject();
        variant.addProperty("model", model);
        for (final String rotation : new String[] {"x", "y"}) {
            if (settings.get(rotation) instanceof final JsonPrimitive number && number.isNumber() && number.getAsInt() % 360 != 0) {
                variant.addProperty(rotation, Math.floorMod(number.getAsInt(), 360));
            }
        }
        if (settings.get("uvlock") instanceof final JsonPrimitive uvlock && uvlock.isBoolean() && uvlock.getAsBoolean()) {
            variant.addProperty("uvlock", true);
        }
        return variant;
    }

    private static void merge(final JsonObject into, final @Nullable JsonElement from) {
        JsonElement element = from;
        if (element instanceof final JsonArray array) {
            element = array.isEmpty() ? null : array.get(0);
        }
        if (element instanceof final JsonObject object) {
            for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
                into.add(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * A 1.12 blockstate's model as a 26.3 model id: JSON models are found under {@code models/block/}; an OBJ model gets a wrapper that loads
     * it with NeoForge's OBJ loader.
     */
    @Nullable String modelReference(final String owner, final String reference) {
        final String namespace = reference.contains(":") ? namespace(reference) : "minecraft";
        // 1.12 read "almura:/light/lamp" as "almura:light/lamp"; 26.3 doesn't
        String path = path(reference);
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        final Path models = this.source.namespace(namespace).resolve("models");
        if (path.endsWith(".obj")) {
            final String objPath = Files.isRegularFile(models.resolve("block").resolve(path)) ? "block/" + path : path;
            if (!Files.isRegularFile(models.resolve(objPath))) {
                this.problems.add(owner + ": no OBJ model models/" + objPath);
                return null;
            }
            return this.objWrapper(namespace, objPath);
        }
        if (namespace.equals("minecraft")) {
            return "minecraft:block/" + path;
        }
        if (Files.isRegularFile(models.resolve("block").resolve(path + ".json"))) {
            return namespace + ":block/" + path;
        }
        if (Files.isRegularFile(models.resolve(path + ".json"))) {
            return namespace + ":" + path;
        }
        this.problems.add(owner + ": no model models/block/" + path + ".json");
        return null;
    }

    /**
     * A model loading an OBJ with NeoForge's loader ({@code models/obj/...}), its material library's textures resolved as 1.12's Forge did:
     * a texture named without a folder is in the model's folder ({@code almura:alfalfa.png} beside {@code models/block/x.obj} is
     * {@code almura:block/alfalfa}).
     *
     * @param objPath the OBJ's path under {@code models/}
     * @return The wrapper's id
     */
    private String objWrapper(final String namespace, final String objPath) {
        final String base = objPath.substring(0, objPath.length() - ".obj".length());
        final String id = namespace + ":obj/" + base;
        final String file = "assets/" + namespace + "/models/obj/" + base + ".json";
        if (this.pack.has(file)) {
            return id;
        }
        final Path models = this.source.namespace(namespace).resolve("models");
        final String folder = objPath.contains("/") ? objPath.substring(0, objPath.lastIndexOf('/') + 1) : "";
        final JsonObject textures = new JsonObject();
        String particle = null;
        for (final String map : this.materialTextures(models.resolve(base + ".mtl"))) {
            final String texture = this.textureOf(namespace, folder, map);
            if (texture != null) {
                textures.addProperty(map, texture);
                if (particle == null) {
                    particle = texture;
                }
            } else {
                this.problems.add(id + ": no texture for '" + map + "'");
            }
        }
        if (particle != null) {
            textures.addProperty("particle", particle);
        }
        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("parent", "minecraft:block/block");
        wrapper.addProperty("loader", "neoforge:obj");
        wrapper.addProperty("model", namespace + ":models/" + objPath);
        wrapper.add("textures", textures);
        this.pack.putJson(file, wrapper);
        return id;
    }

    private List<String> materialTextures(final Path mtl) {
        final Set<String> maps = new TreeSet<>();
        if (!Files.isRegularFile(mtl)) {
            return List.of();
        }
        try {
            for (final String line : Files.readAllLines(mtl, StandardCharsets.UTF_8)) {
                final String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2 && parts[0].equals("map_Kd")) {
                    maps.add(parts[parts.length - 1]);
                }
            }
        } catch (final IOException e) {
            this.problems.add(mtl + ": couldn't read: " + e.getMessage());
        }
        return List.copyOf(maps);
    }

    /**
     * 1.12 Forge's reading of a material texture: {@code ns:path.png}, a path without a folder being in the model's folder; the texture as a
     * 26.3 id, if it exists.
     */
    private @Nullable String textureOf(final String modelNamespace, final String modelFolder, final String map) {
        String namespace = modelNamespace, path = map;
        if (map.contains(":")) {
            namespace = namespace(map);
            path = path(map);
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        final Path textures = this.source.namespace(namespace).resolve("textures");
        for (final String candidate : path.contains("/") ? List.of(path, modelFolder + path) : List.of(modelFolder + path, "block/" + path, path)) {
            if (Files.isRegularFile(textures.resolve(candidate + ".png"))) {
                return namespace + ":" + candidate;
            }
        }
        return namespace.equals("minecraft") ? Legacy.texture("minecraft:" + path) : null;
    }

    /**
     * 26.3's item model definition ({@code items/<path>.json}): the item's model ({@code models/item/<path>.json}), or for a block item the
     * block's.
     */
    private void itemModel(final ItemDef item) {
        final String namespace = namespace(item.id()), path = path(item.id());
        final Path models = this.source.namespace(namespace).resolve("models");
        final String model;
        if (Files.isRegularFile(models.resolve("item").resolve(path + ".json"))) {
            model = namespace + ":item/" + path;
        } else {
            this.problems.add(item.id() + ": no item model (models/item/" + path + ".json)");
            return;
        }
        this.itemModelDefinition(namespace, path, model);
    }

    /**
     * A block item's model definition: the model of the block's default (or north facing) variant.
     */
    private void blockItemModel(final BlockDef block) {
        final String namespace = namespace(block.id()), path = path(block.id());
        final JsonElement blockstate = this.pack.json("assets/" + namespace + "/blockstates/" + path + ".json");
        if (blockstate instanceof final JsonObject object && object.get("variants") instanceof final JsonObject variants && !variants.isEmpty()) {
            final JsonObject first = variants.has("facing=north") ? variants.getAsJsonObject("facing=north")
                : variants.entrySet().iterator().next().getValue().getAsJsonObject();
            this.itemModelDefinition(namespace, path, first.get("model").getAsString());
        }
    }

    private void itemModelDefinition(final String namespace, final String path, final String model) {
        final JsonObject inner = new JsonObject();
        inner.addProperty("type", "minecraft:model");
        inner.addProperty("model", model);
        final JsonObject definition = new JsonObject();
        definition.add("model", inner);
        this.pack.putJson("assets/" + namespace + "/items/" + path + ".json", definition);
    }

    /**
     * A block's loot table: its drops (with their bonus as a separate chance), or itself when it names none.
     */
    private void lootTable(final BlockDef block) {
        final String namespace = namespace(block.id()), path = path(block.id());
        final JsonArray pools = new JsonArray();
        final List<DropDef> drops = block.state().drops().isEmpty() ? List.of(new DropDef(block.id(), 1, 1, 0, 0)) : block.state().drops();
        for (final DropDef drop : drops) {
            pools.add(pool(drop.item(), drop.amountMin(), drop.amountMax(), 0));
            if (drop.bonusAmount() > 0 && drop.bonusChance() > 0) {
                pools.add(pool(drop.item(), drop.bonusAmount(), drop.bonusAmount(), drop.bonusChance()));
            }
        }
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace + ":blocks/" + path);
        this.pack.putJson("data/" + namespace + "/loot_table/blocks/" + path + ".json", table);
    }

    private static JsonObject pool(final String item, final int min, final int max, final int chancePercent) {
        final JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        if (min != 1 || max != 1) {
            final JsonObject count = new JsonObject();
            if (min == max) {
                count.addProperty("count", min);
            } else {
                final JsonObject range = new JsonObject();
                range.addProperty("type", "minecraft:uniform");
                range.addProperty("min", min);
                range.addProperty("max", max);
                count.add("count", range);
            }
            count.addProperty("function", "minecraft:set_count");
            final JsonArray functions = new JsonArray();
            functions.add(count);
            entry.add("functions", functions);
        }
        final JsonArray entries = new JsonArray();
        entries.add(entry);
        final JsonArray conditions = new JsonArray();
        final JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(survives);
        if (chancePercent > 0) {
            final JsonObject chance = new JsonObject();
            chance.addProperty("condition", "minecraft:random_chance");
            chance.addProperty("chance", chancePercent / 100.0);
            conditions.add(chance);
        }
        final JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        pool.add("conditions", conditions);
        return pool;
    }

    /**
     * {@code mineable/pickaxe} and its kin from the blocks' effective tools, and the tier the cheapest of them needs.
     */
    private void toolTags(final List<BlockDef> blocks) {
        final Map<String, Set<String>> tags = new TreeMap<>();
        for (final BlockDef block : blocks) {
            for (final String tool : block.effectiveTools()) {
                final String kind = toolKind(tool);
                if (kind != null) {
                    tags.computeIfAbsent("minecraft:mineable/" + kind, key -> new TreeSet<>()).add(block.id());
                }
            }
            final String tier = neededTier(block);
            if (tier != null) {
                tags.computeIfAbsent("minecraft:needs_" + tier + "_tool", key -> new TreeSet<>()).add(block.id());
            }
        }
        tags.forEach((tag, values) -> {
            final JsonArray array = new JsonArray();
            values.forEach(array::add);
            final JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", array);
            this.pack.putJson("data/" + namespace(tag) + "/tags/block/" + path(tag) + ".json", json);
        });
    }

    static @Nullable String toolKind(final String tool) {
        final String path = path(tool);
        for (final String kind : new String[] {"pickaxe", "axe", "shovel", "hoe", "sword", "shears"}) {
            if (path.endsWith("_" + kind) || path.equals(kind)) {
                return kind.equals("sword") || kind.equals("shears") ? null : kind;
            }
        }
        return null;
    }

    /**
     * The lowest tool tier among the effective tools, when it is above wood: {@code stone}, {@code iron} or {@code diamond}.
     */
    static @Nullable String neededTier(final BlockDef block) {
        int lowest = Integer.MAX_VALUE;
        for (final String tool : block.effectiveTools()) {
            final String path = path(tool);
            final int tier = path.startsWith("wooden_") || path.startsWith("golden_") ? 0 : path.startsWith("stone_") ? 1
                : path.startsWith("iron_") ? 2 : path.startsWith("diamond_") || path.startsWith("netherite_") ? 3 : 0;
            lowest = Math.min(lowest, tier);
        }
        return switch (lowest == Integer.MAX_VALUE ? 0 : lowest) {
            case 1 -> "stone";
            case 2 -> "iron";
            case 3 -> "diamond";
            default -> null;
        };
    }

    /**
     * {@code lang/en_us.json}: item and block names and tooltips from the content's translations, and the creative tabs' titles.
     */
    private void language(final List<ItemGroupDef> groups) {
        final Map<String, Map<String, String>> byNamespace = new TreeMap<>();
        for (final String type : new String[] {"item", "block"}) {
            for (final TranslationFile file : this.source.translations(type, "en_us")) {
                final Map<String, String> lang = byNamespace.computeIfAbsent(file.namespace(), key -> new TreeMap<>());
                for (final Map.Entry<String, String> entry : file.entries().entrySet()) {
                    final String key = entry.getKey();
                    final int dot = key.lastIndexOf('.');
                    final String name = dot < 0 ? key : key.substring(0, dot);
                    final String suffix = dot < 0 ? "" : key.substring(dot + 1);
                    final String id = (file.folder() + name).replace('/', '.');
                    final String base = type + "." + file.namespace() + "." + id;
                    if (suffix.equals("name") || suffix.isEmpty()) {
                        lang.put(base, entry.getValue());
                    } else {
                        lang.put(base + "." + suffix, entry.getValue());
                    }
                }
            }
        }
        for (final TranslationFile file : this.source.translations("item_group", "en_us")) {
            final Map<String, String> lang = byNamespace.computeIfAbsent(file.namespace(), key -> new TreeMap<>());
            file.entries().forEach((name, value) -> lang.put(itemGroupKey(file.namespace() + ":" + file.folder() + name), value));
        }
        for (final ItemGroupDef group : groups) {
            byNamespace.computeIfAbsent(namespace(group.id()), key -> new TreeMap<>()).putIfAbsent(itemGroupKey(group.id()), path(group.id()));
        }
        byNamespace.forEach((namespace, lang) -> {
            final JsonObject json = new JsonObject();
            lang.forEach(json::addProperty);
            this.pack.putJson("assets/" + namespace + "/lang/en_us.json", json);
        });
    }

    public static String itemGroupKey(final String id) {
        return "itemGroup." + namespace(id) + "." + path(id).replace('/', '.');
    }

    /**
     * The content's recipes in 26.3's format, under {@code data/<namespace>/recipe/}. One that uses an item 26.3 doesn't have is left out.
     */
    private void recipes(final Predicate<String> knownItem) {
        for (final String type : new String[] {"item", "block"}) {
            for (final ContentFile file : this.source.recipes(type)) {
                final String problem = RecipeConverter.problem(file.json(), knownItem);
                final JsonObject recipe = problem == null ? RecipeConverter.convert(file.json()) : null;
                if (recipe == null) {
                    this.problems.add(file.id() + ": recipe left out: " + (problem == null ? "unknown type" : problem));
                    continue;
                }
                this.pack.putJson("data/" + file.namespace() + "/recipe/" + file.path() + ".json", recipe);
            }
        }
    }

    static String namespace(final String id) {
        final int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }

    static String path(final String id) {
        final int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    /**
     * The files as a map, for tests: generated JSON and the source of passed-through files.
     */
    public Map<String, Object> describe() {
        final Map<String, Object> files = new LinkedHashMap<>();
        this.pack.paths().forEach(path -> files.put(path, this.pack.json(path) != null ? this.pack.json(path) : this.pack.file(path)));
        return files;
    }
}
