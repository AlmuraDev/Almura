/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.convert;

import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.CropDef;
import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.GrassGenDef;
import com.almuradev.almura.content.def.ContentDefs.OreDef;
import com.almuradev.almura.content.def.ContentDefs.OreGenDef;
import com.almuradev.almura.content.def.ContentDefs.TreeGenDef;
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
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

import javax.imageio.ImageIO;

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

    private final Map<String, CropDef> crops = new LinkedHashMap<>();
    private List<TreeGenDef> treeGeneration = List.of();
    private List<GrassGenDef> grassGeneration = List.of();
    private List<OreGenDef> oreGeneration = List.of();

    public ContentPackBuilder(final ContentSource source) {
        this.source = source;
    }

    /**
     * The crops among the blocks, for their stages: a blockstate variant and loot for each.
     */
    public ContentPackBuilder crops(final List<CropDef> crops) {
        crops.forEach(crop -> this.crops.put(crop.id(), crop));
        return this;
    }

    /**
     * The world generation: its features and the biome modifier adding them to the overworld's biomes.
     */
    public ContentPackBuilder generation(final List<TreeGenDef> trees, final List<GrassGenDef> grasses, final List<OreGenDef> ores) {
        this.treeGeneration = trees;
        this.grassGeneration = grasses;
        this.oreGeneration = ores;
        return this;
    }

    /**
     * Builds the client's half of the pack: textures, models, blockstates, item model definitions and the language file.
     */
    public ContentPack buildAssets(final List<ItemGroupDef> groups, final List<ItemDef> items, final List<BlockDef> blocks) {
        for (final String namespace : this.source.namespaces()) {
            this.passThrough(namespace);
        }
        final Map<String, BlockDef> doubleSlabs = new java.util.HashMap<>();
        blocks.stream().filter(BlockDef::isDoubleSlab).forEach(block -> doubleSlabs.put(block.single(), block));
        for (final BlockDef block : blocks) {
            this.blockstate(block, doubleSlabs);
            this.blockItemModel(block);
        }
        for (final ItemDef item : items) {
            // A slab's item is its block's, with the block's item model
            if (!item.kind().equals("slab")) {
                this.itemModel(item);
            }
        }
        this.language(groups);
        return this.pack;
    }

    /**
     * Builds the server's half of the pack: loot tables, tool tags and recipes.
     *
     * @param knownItem whether an item id exists, for recipes: one using an unknown item is left out
     */
    public ContentPack buildData(final List<BlockDef> blocks, final Predicate<String> knownItem) {
        return this.buildData(blocks, List.of(), knownItem);
    }

    /**
     * @param items for the sickles' tag of the blocks they cut quickly
     */
    public ContentPack buildData(final List<BlockDef> blocks, final List<ItemDef> items, final Predicate<String> knownItem) {
        for (final BlockDef block : blocks) {
            this.lootTable(block, knownItem);
        }
        this.cropTags(blocks);
        this.treeTags(blocks);
        this.worldGeneration(knownBlock(blocks));
        this.toolTags(blocks);
        this.sickleTag(items);
        this.recipes(knownItem);
        return this.pack;
    }

    /**
     * Builds both halves, as tests do.
     */
    public ContentPack build(final List<ItemGroupDef> groups, final List<ItemDef> items, final List<BlockDef> blocks,
        final Predicate<String> knownItem) {
        this.buildAssets(groups, items, blocks);
        return this.buildData(blocks, items, knownItem);
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
                    this.pack.putJson(path, model(model, file.startsWith("models/block/")));
                }
            } else {
                this.pack.putFile(path, folder.resolve(file));
            }
        }
    }

    /**
     * A 1.12 JSON model as 26.3 reads it: vanilla textures renamed ({@code blocks/dirt} as {@code block/dirt}); a block model with no parent
     * given {@code block/block}'s display (Forge's {@code forge:default-block}, which every such blockstate names).
     */
    static JsonObject model(final JsonObject model, final boolean block) {
        final JsonObject copy = model.deepCopy();
        if (block && !copy.has("parent") && copy.has("elements")) {
            copy.addProperty("parent", "minecraft:block/block");
        }
        if (copy.get("parent") instanceof final JsonPrimitive parent && parent.isString()) {
            copy.addProperty("parent", Legacy.model(parent.getAsString()));
        }
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
     * The block's 26.3 blockstate from its 1.12 Forge one, with a variant for each of the block's states: its facing (horizontal blocks),
     * its axis (logs), its facing, half and shape (stairs, already written out in full), bottom, top and double (slabs, the double from the
     * double slab's blockstate), or the single default variant.
     */
    private void blockstate(final BlockDef block, final Map<String, BlockDef> doubleSlabs) {
        if (block.isDoubleSlab()) {
            // Part of its single slab in 26.3
            return;
        }
        final JsonObject forge = this.forgeBlockstate(block.id());
        if (forge == null) {
            return;
        }
        final JsonObject variants = new JsonObject();
        switch (block.kind()) {
            case "horizontal", "container" -> {
                for (final String facing : FACINGS) {
                    this.addVariant(variants, "facing=" + facing, this.variant(block, forge, Map.of("facing", facing), "facing=" + facing));
                }
            }
            case "crop" -> {
                final CropDef crop = this.crops.get(block.id());
                for (int age = 0; crop != null && age <= crop.maxAge(); age++) {
                    this.addVariant(variants, "age=" + age, this.variant(block, forge, Map.of("age", Integer.toString(age)), "age=" + age));
                }
            }
            case "log" -> {
                for (final String axis : new String[] {"x", "y", "z"}) {
                    this.addVariant(variants, "axis=" + axis, this.variant(block, forge, Map.of("axis", axis), "axis=" + axis));
                }
            }
            case "stair" -> {
                if (forge.get("variants") instanceof final JsonObject forgeVariants) {
                    for (final String key : forgeVariants.keySet()) {
                        if (key.contains("=")) {
                            this.addVariant(variants, key, this.variant(block, forge, Map.of(), key));
                        }
                    }
                }
            }
            case "slab" -> {
                this.addVariant(variants, "type=bottom", this.variant(block, forge, Map.of(), "half=bottom"));
                this.addVariant(variants, "type=top", this.variant(block, forge, Map.of(), "half=top"));
                final BlockDef doubleSlab = doubleSlabs.get(block.id());
                final JsonObject doubleForge = doubleSlab == null ? null : this.forgeBlockstate(doubleSlab.id());
                if (doubleForge != null) {
                    this.addVariant(variants, "type=double", this.variant(doubleSlab, doubleForge, Map.of(), "normal"));
                } else {
                    this.problems.add(block.id() + ": no double slab to show when doubled");
                }
            }
            default -> this.addVariant(variants, "", this.variant(block, forge, Map.of(), "normal"));
        }
        final JsonObject blockstate = new JsonObject();
        blockstate.add("variants", variants);
        this.pack.putJson("assets/" + namespace(block.id()) + "/blockstates/" + path(block.id()) + ".json", blockstate);
    }

    private @Nullable JsonObject forgeBlockstate(final String id) {
        final String path = path(id);
        final Path file = this.source.namespace(namespace(id)).resolve("blockstates").resolve(path + ".json");
        if (!Files.isRegularFile(file)) {
            this.problems.add(id + ": no blockstate (blockstates/" + path + ".json)");
            return null;
        }
        return this.source.readObject(file);
    }

    private void addVariant(final JsonObject variants, final String key, final @Nullable JsonObject variant) {
        if (variant != null) {
            variants.add(key, variant);
        }
    }

    /**
     * One variant: Forge's defaults, then each property value's settings ({@code "facing": {"south": {"y": 180}}}), then the full
     * variant's ({@code "facing=south"} or {@code "normal"}).
     */
    private @Nullable JsonObject variant(final BlockDef block, final JsonObject forge, final Map<String, String> properties,
        final String fullKey) {
        final JsonObject settings = new JsonObject();
        merge(settings, forge.get("defaults"));
        final JsonObject variants = forge.get("variants") instanceof final JsonObject object ? object : new JsonObject();
        properties.forEach((property, value) -> {
            if (variants.get(property) instanceof final JsonObject values) {
                merge(settings, values.get(value));
            }
        });
        merge(settings, variants.get(fullKey));
        if (!settings.has("model")) {
            this.problems.add(block.id() + ": the blockstate names no model for " + fullKey);
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
            return Legacy.model("minecraft:block/" + path);
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
     * A model loading an OBJ with NeoForge's loader ({@code models/obj/...}). The 4.0 content's OBJs are as NeoForge reads them: the
     * material library named by its full path ({@code mtllib almura:models/block/x.mtl}), texture coordinates the right way up, the
     * library's textures named by id ({@code map_Kd almura:block/x}), each of which is a texture slot of the same name here.
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
        final Path mtl = this.materialLibrary(id, namespace, objPath);
        final JsonObject textures = new JsonObject();
        String particle = null;
        for (final String texture : mtl == null ? List.<String>of() : this.materialTextures(mtl)) {
            if (!this.textureExists(texture)) {
                this.problems.add(id + ": texture " + texture + " isn't there");
            }
            textures.addProperty(texture, texture);
            if (particle == null) {
                particle = texture;
            }
        }
        // As 3.5's OBJ reader did: the particles show the part of the sheet the model's last face uses, not the whole sheet
        final String cut = mtl == null ? null : this.objParticle(namespace, objPath, base, mtl);
        if (cut != null) {
            particle = cut;
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

    /**
     * The OBJ's material library: the file its {@code mtllib} names ({@code almura:models/block/x.mtl}), or null (a problem) when it names
     * none or one that isn't there.
     */
    private @Nullable Path materialLibrary(final String owner, final String namespace, final String objPath) {
        try {
            for (final String line : Files.readAllLines(this.source.namespace(namespace).resolve("models").resolve(objPath), StandardCharsets.UTF_8)) {
                final String trimmed = line.trim();
                if (trimmed.startsWith("mtllib ")) {
                    final String named = trimmed.substring("mtllib ".length()).trim();
                    final Path library = named.contains(":") ? this.source.namespace(namespace(named)).resolve(path(named)) : null;
                    if (library == null || !Files.isRegularFile(library)) {
                        this.problems.add(owner + ": material library " + named + " isn't there");
                        return null;
                    }
                    return library;
                }
            }
        } catch (final IOException e) {
            this.problems.add(owner + ": couldn't read: " + e.getMessage());
            return null;
        }
        this.problems.add(owner + ": names no material library");
        return null;
    }

    private boolean textureExists(final String texture) {
        return texture.contains(":") && Files.isRegularFile(this.source.namespace(namespace(texture)).resolve("textures").resolve(path(texture) + ".png"));
    }

    /**
     * The OBJ's particle texture, as 3.5's OBJ reader made it: the part of its texture sheet between the UVs of the first and third corners
     * of its last face, here cut out and scaled to 16x16 ({@code textures/block/obj_particle/...}).
     *
     * @return The texture's id, or null if the face or its texture can't be found
     */
    private @Nullable String objParticle(final String namespace, final String objPath, final String base, final Path mtl) {
        final Path models = this.source.namespace(namespace).resolve("models");
        final List<float[]> uvs = new ArrayList<>();
        String material = null, faceMaterial = null;
        String[] face = null;
        try {
            for (final String line : Files.readAllLines(models.resolve(objPath), StandardCharsets.UTF_8)) {
                final String[] parts = line.trim().split("\\s+");
                switch (parts[0]) {
                    case "vt" -> {
                        if (parts.length >= 3) {
                            uvs.add(new float[] {Float.parseFloat(parts[1]), Float.parseFloat(parts[2])});
                        }
                    }
                    case "usemtl" -> material = parts.length >= 2 ? parts[1] : null;
                    case "f" -> {
                        if (parts.length >= 4) {
                            face = parts;
                            faceMaterial = material;
                        }
                    }
                    default -> { }
                }
            }
        } catch (final IOException | NumberFormatException e) {
            return null;
        }
        if (face == null || faceMaterial == null) {
            return null;
        }
        final float[] first = uv(uvs, face[1]), third = uv(uvs, face[3]);
        final String texture = this.materialMap(mtl, faceMaterial);
        if (first == null || third == null || texture == null || !this.textureExists(texture)) {
            return null;
        }
        final Path image = this.source.namespace(namespace(texture)).resolve("textures").resolve(path(texture) + ".png");
        final String id = namespace + ":block/obj_particle/" + base;
        this.pack.putGenerated("assets/" + namespace + "/textures/block/obj_particle/" + base + ".png", () -> cut(image, first, third));
        return id;
    }

    /** The UV of a face corner ({@code v/vt/vn}, the vt counted from 1, or back from the end when negative). */
    private static float @Nullable [] uv(final List<float[]> uvs, final String corner) {
        final String[] indices = corner.split("/");
        if (indices.length < 2 || indices[1].isEmpty()) {
            return null;
        }
        try {
            final int index = Integer.parseInt(indices[1]);
            final int at = index < 0 ? uvs.size() + index : index - 1;
            return at >= 0 && at < uvs.size() ? uvs.get(at) : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /** The texture ({@code map_Kd}) of one material of a material library. */
    private @Nullable String materialMap(final Path mtl, final String material) {
        if (!Files.isRegularFile(mtl)) {
            return null;
        }
        try {
            String current = null;
            for (final String line : Files.readAllLines(mtl, StandardCharsets.UTF_8)) {
                final String[] parts = line.trim().split("\\s+");
                if (parts[0].equals("newmtl") && parts.length >= 2) {
                    current = parts[1];
                } else if (parts[0].equals("map_Kd") && parts.length >= 2 && material.equals(current)) {
                    return parts[parts.length - 1];
                }
            }
        } catch (final IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Cuts the part of an image between two UVs and scales it to 16x16, a size that keeps the block atlas's mipmaps; an animated texture's
     * first frame is used.
     */
    private static byte @Nullable [] cut(final Path image, final float[] a, final float[] b) {
        try {
            final BufferedImage sheet = ImageIO.read(image.toFile());
            if (sheet == null) {
                return null;
            }
            final int width = sheet.getWidth();
            // An animation's frames are stacked; its first is square
            final int height = Files.isRegularFile(image.resolveSibling(image.getFileName() + ".mcmeta")) ? Math.min(width, sheet.getHeight())
                : sheet.getHeight();
            final int x1 = clamp(Math.round(Math.min(a[0], b[0]) * width), width - 1);
            final int x2 = clamp(Math.round(Math.max(a[0], b[0]) * width), width);
            final int y1 = clamp(Math.round(Math.min(a[1], b[1]) * height), height - 1);
            final int y2 = clamp(Math.round(Math.max(a[1], b[1]) * height), height);
            final BufferedImage region = sheet.getSubimage(x1, y1, Math.max(1, x2 - x1), Math.max(1, y2 - y1));
            final BufferedImage scaled = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.drawImage(region, 0, 0, 16, 16, null);
            graphics.dispose();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(scaled, "png", out);
            return out.toByteArray();
        } catch (final IOException | RuntimeException e) {
            return null;
        }
    }

    private static int clamp(final int value, final int max) {
        return Math.max(0, Math.min(max, value));
    }

    /** The textures ({@code map_Kd}) of a material library. */
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
     * A block item's model definition: its own item model if the content has one ({@code models/item/<path>.json}, as slabs do), else the
     * model of the block's default variant (north facing, upright, bottom, or the first).
     */
    private void blockItemModel(final BlockDef block) {
        // A crop's item is its seed
        if (block.isDoubleSlab() || block.kind().equals("crop")) {
            return;
        }
        final String namespace = namespace(block.id()), path = path(block.id());
        if (Files.isRegularFile(this.source.namespace(namespace).resolve("models").resolve("item").resolve(path + ".json"))) {
            this.itemModelDefinition(namespace, path, namespace + ":item/" + path);
            return;
        }
        final JsonElement blockstate = this.pack.json("assets/" + namespace + "/blockstates/" + path + ".json");
        if (blockstate instanceof final JsonObject object && object.get("variants") instanceof final JsonObject variants && !variants.isEmpty()) {
            JsonObject chosen = null;
            for (final String key : new String[] {"facing=north", "axis=y", "type=bottom", "facing=east,half=bottom,shape=straight", ""}) {
                if (variants.get(key) instanceof final JsonObject variant) {
                    chosen = variant;
                    break;
                }
            }
            if (chosen == null) {
                chosen = variants.entrySet().iterator().next().getValue().getAsJsonObject();
            }
            final String model = chosen.get("model").getAsString();
            final JsonObject display = this.inventoryDisplay(block);
            if (display == null) {
                this.itemModelDefinition(namespace, path, model);
            } else {
                // The 1.12 blockstate placed the item its own way (flags, smaller in the slot and the hand)
                final JsonObject itemModel = new JsonObject();
                itemModel.addProperty("parent", model);
                itemModel.add("display", display);
                this.pack.putJson("assets/" + namespace + "/models/block_item/" + path + ".json", itemModel);
                this.itemModelDefinition(namespace, path, namespace + ":block_item/" + path);
            }
        }
    }

    /**
     * The display a 1.12 blockstate gave its block's item ({@code "inventory": [{"transform": {"gui": {...}, ...}}]}) as 26.3 model
     * display, or null when it named Forge's {@code forge:default-block} (the block model's own) or nothing. Forge's rotations are a list of
     * single-axis turns, applied in order; its translations are in blocks, 26.3's in sixteenths.
     */
    private @Nullable JsonObject inventoryDisplay(final BlockDef block) {
        final JsonObject forge = this.forgeBlockstate(block.id());
        if (forge == null) {
            return null;
        }
        final JsonObject settings = new JsonObject();
        merge(settings, forge.get("defaults"));
        if (forge.get("variants") instanceof final JsonObject variants) {
            merge(settings, variants.get("inventory"));
        }
        if (!(settings.get("transform") instanceof final JsonObject transform)) {
            return null;
        }
        final JsonObject display = new JsonObject();
        for (final Map.Entry<String, JsonElement> entry : transform.entrySet()) {
            final String context = switch (entry.getKey()) {
                case "thirdperson" -> "thirdperson_righthand";
                case "firstperson" -> "firstperson_righthand";
                default -> entry.getKey();
            };
            if (!(entry.getValue() instanceof final JsonObject forgeTransform)) {
                continue;
            }
            final JsonObject converted = new JsonObject();
            final JsonArray rotation = this.forgeRotation(block.id(), forgeTransform.get("rotation"));
            if (rotation != null) {
                converted.add("rotation", rotation);
            }
            if (forgeTransform.get("translation") instanceof final JsonArray translation && translation.size() == 3) {
                final JsonArray sixteenths = new JsonArray();
                translation.forEach(value -> sixteenths.add(value.getAsDouble() * 16.0));
                converted.add("translation", sixteenths);
            }
            if (forgeTransform.get("scale") instanceof final JsonPrimitive uniform && uniform.isNumber()) {
                final JsonArray scales = new JsonArray();
                for (int i = 0; i < 3; i++) {
                    scales.add(uniform.getAsDouble());
                }
                converted.add("scale", scales);
            } else if (forgeTransform.get("scale") instanceof final JsonArray scales && scales.size() == 3) {
                converted.add("scale", scales.deepCopy());
            }
            display.add(context, converted);
        }
        return display.isEmpty() ? null : display;
    }

    /** Forge's rotation ({@code [{"x": 30}, {"y": 225}, {"z": 0}]} or {@code [x, y, z]}) as 26.3's {@code [x, y, z]}. */
    private @Nullable JsonArray forgeRotation(final String owner, final @Nullable JsonElement rotation) {
        if (!(rotation instanceof final JsonArray list)) {
            return null;
        }
        if (list.size() == 3 && list.get(0).isJsonPrimitive()) {
            return list.deepCopy();
        }
        final double[] xyz = new double[3];
        int last = -1;
        for (final JsonElement turn : list) {
            if (!(turn instanceof final JsonObject object) || object.size() != 1) {
                this.problems.add(owner + ": inventory rotation not understood: " + rotation);
                return null;
            }
            final String axis = object.keySet().iterator().next();
            final int index = "xyz".indexOf(axis);
            // 26.3 turns about x, then y, then z; a list in another order can't be written that way
            if (index < 0 || index <= last && object.get(axis).getAsDouble() != 0) {
                this.problems.add(owner + ": inventory rotation not in x, y, z order: " + rotation);
                return null;
            }
            xyz[index] = object.get(axis).getAsDouble();
            last = Math.max(last, index);
        }
        final JsonArray converted = new JsonArray();
        for (final double value : xyz) {
            converted.add(value);
        }
        return converted;
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
     * A block's loot table: its drops (with their bonus as a separate chance), or itself when it names none. A drop naming an item that
     * doesn't exist is a mistake in the content: left out, and a problem the tests fail on.
     */
    private void lootTable(final BlockDef block, final Predicate<String> knownItem) {
        if (block.isDoubleSlab()) {
            return;
        }
        final String namespace = namespace(block.id()), path = path(block.id());
        if (block.kind().equals("slab")) {
            this.slabLootTable(block);
            return;
        }
        if (block.kind().equals("crop")) {
            this.cropLootTable(block, knownItem);
            return;
        }
        if (block.kind().equals("leaf")) {
            this.leafLootTable(block, knownItem);
            return;
        }
        if (block.kind().equals("container")) {
            this.containerLootTable(block);
            return;
        }
        final JsonArray pools = new JsonArray();
        List<DropDef> drops = new ArrayList<>();
        for (final DropDef drop : block.state().drops()) {
            if (knownItem.test(drop.item())) {
                drops.add(drop);
            } else {
                this.problems.add(block.id() + ": drops " + drop.item() + ", which doesn't exist; left out");
            }
        }
        // A block that lists no drops drops itself, as on 1.12
        if (block.state().drops().isEmpty()) {
            drops = List.of(new DropDef(block.id(), 1, 1, 0, 0));
        }
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

    /**
     * A crop drops what its stage lists (the produce when ripe) and nothing at a stage that lists nothing, as 3.5's crops did: not even
     * the seed (seeds come from grass and recipes).
     */
    private void cropLootTable(final BlockDef block, final Predicate<String> knownItem) {
        final CropDef crop = this.crops.get(block.id());
        final JsonArray pools = new JsonArray();
        for (final CropStageDef stage : crop == null ? List.<CropStageDef>of() : crop.stages()) {
            for (final DropDef drop : stage.state().drops()) {
                if (!knownItem.test(drop.item())) {
                    this.problems.add(block.id() + ": drops " + drop.item() + " at age " + stage.age() + ", which doesn't exist; left out");
                    continue;
                }
                final JsonObject atAge = matchBlock(block.id(), "age", Integer.toString(stage.age()));
                pools.add(pool(drop.item(), drop.amountMin(), drop.amountMax(), 0, atAge));
                if (drop.bonusAmount() > 0 && drop.bonusChance() > 0) {
                    pools.add(pool(drop.item(), drop.bonusAmount(), drop.bonusAmount(), drop.bonusChance(), atAge.deepCopy()));
                }
            }
        }
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace(block.id()) + ":blocks/" + path(block.id()));
        this.pack.putJson("data/" + namespace(block.id()) + "/loot_table/blocks/" + path(block.id()) + ".json", table);
    }

    /**
     * Leaves give themselves to shears or silk touch, as 1.12's did; otherwise what the content lists (a sapling at a chance, fruit),
     * and nothing when it lists nothing. Their decay drops are {@code ContentLeaves}'s.
     */
    private void leafLootTable(final BlockDef block, final Predicate<String> knownItem) {
        final JsonArray pools = new JsonArray();
        final JsonObject shearsOrSilk = new JsonObject();
        shearsOrSilk.addProperty("type", "minecraft:any_of");
        final JsonArray tools = new JsonArray();
        tools.add("minecraft:tool/can_shear");
        tools.add("minecraft:tool/can_silk_touch");
        shearsOrSilk.add("terms", tools);
        final JsonObject itself = pool(block.id(), 1, 1, 0, shearsOrSilk);
        pools.add(itself);
        final JsonObject otherwise = new JsonObject();
        otherwise.addProperty("type", "minecraft:inverted");
        otherwise.add("term", shearsOrSilk.deepCopy());
        for (final DropDef drop : block.state().drops()) {
            if (!knownItem.test(drop.item())) {
                this.problems.add(block.id() + ": drops " + drop.item() + ", which doesn't exist; left out");
                continue;
            }
            // "amount": 0 with a bonus is a chance of the bonus alone (a sapling a quarter of the time)
            if (drop.amountMax() > 0) {
                pools.add(pool(drop.item(), drop.amountMin(), drop.amountMax(), 0, otherwise.deepCopy()));
            }
            if (drop.bonusAmount() > 0 && drop.bonusChance() > 0) {
                pools.add(pool(drop.item(), drop.bonusAmount(), drop.bonusAmount(), drop.bonusChance(), otherwise.deepCopy()));
            }
        }
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace(block.id()) + ":blocks/" + path(block.id()));
        this.pack.putJson("data/" + namespace(block.id()) + "/loot_table/blocks/" + path(block.id()) + ".json", table);
    }

    /**
     * A container drops itself holding what it held (and its name), as a shulker box does; broken while sneaking it spilled first
     * ({@code ContentContainers}).
     */
    private void containerLootTable(final BlockDef block) {
        final JsonObject copy = new JsonObject();
        copy.addProperty("type", "minecraft:copy_components");
        copy.addProperty("source", "block_entity");
        final JsonArray include = new JsonArray();
        include.add("minecraft:custom_name");
        include.add("minecraft:container");
        copy.add("include", include);
        final JsonObject pool = pool(block.id(), 1, 1, 0);
        final JsonArray modifiers = new JsonArray();
        modifiers.add(copy);
        pool.getAsJsonArray("entries").get(0).getAsJsonObject().add("modifier", modifiers);
        final JsonArray pools = new JsonArray();
        pools.add(pool);
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace(block.id()) + ":blocks/" + path(block.id()));
        this.pack.putJson("data/" + namespace(block.id()) + "/loot_table/blocks/" + path(block.id()) + ".json", table);
    }

    /**
     * The content's logs are logs to the game ({@code minecraft:logs_that_burn}, so {@code minecraft:logs}: what keeps leaves from
     * decaying), its leaves leaves (shears and hoes cut them quickly), its saplings saplings.
     */
    private void treeTags(final List<BlockDef> blocks) {
        final Map<String, String> tags = Map.of("log", "logs_that_burn", "leaf", "leaves", "sapling", "saplings");
        tags.forEach((kind, tag) -> {
            final JsonArray values = new JsonArray();
            blocks.stream().filter(block -> block.kind().equals(kind)).forEach(block -> values.add(block.id()));
            if (!values.isEmpty()) {
                final JsonObject json = new JsonObject();
                json.addProperty("replace", false);
                json.add("values", values);
                this.pack.putJson("data/minecraft/tags/block/" + tag + ".json", json);
                // The items too, for recipes and fuel
                this.pack.putJson("data/minecraft/tags/item/" + tag + ".json", json.deepCopy());
            }
        });
    }

    private static Predicate<String> knownBlock(final List<BlockDef> blocks) {
        final Set<String> ids = new TreeSet<>();
        blocks.forEach(block -> ids.add(block.id()));
        return ids::contains;
    }

    /**
     * World generation: Almura's trees and grass ({@code almura:content_trees}, {@code almura:content_grass}, code features, see
     * {@code ContentGeneration}) in the step before vanilla's vegetation, so they go first and can hold vanilla's back; the ores as
     * vanilla ore features, each a count of veins of a size a chunk, evenly from height 0 to 63 (1.12's {@code WorldGenMinable} in
     * stone). All for the overworld's biomes ({@code data/almura/neoforge/biome_modifier/}).
     */
    private void worldGeneration(final Predicate<String> knownBlock) {
        final List<String> early = new ArrayList<>();
        if (!this.treeGeneration.isEmpty()) {
            early.add(this.codeFeature("content_trees"));
        }
        if (!this.grassGeneration.isEmpty()) {
            early.add(this.codeFeature("content_grass"));
        }
        if (!early.isEmpty()) {
            this.biomeModifier("content_vegetation", early, "fluid_springs");
        }
        final List<String> ores = new ArrayList<>();
        for (final OreGenDef generation : this.oreGeneration) {
            if (!generation.dimension().equals("overworld")) {
                this.problems.add("ore generation for " + generation.dimension() + ": only the overworld's is placed");
                continue;
            }
            for (final OreDef ore : generation.ores()) {
                if (!knownBlock.test(ore.block())) {
                    this.problems.add("ore generation: " + ore.block() + " isn't a block; left out");
                    continue;
                }
                final String name = "ore_" + path(ore.block()).replace('/', '_');
                final JsonObject target = new JsonObject();
                target.addProperty("predicate_type", "minecraft:tag_match");
                target.addProperty("tag", "minecraft:stone_ore_replaceables");
                final JsonObject entry = new JsonObject();
                entry.addProperty("state", ore.block());
                entry.add("target", target);
                final JsonArray targets = new JsonArray();
                targets.add(entry);
                final JsonObject feature = new JsonObject();
                feature.addProperty("type", "minecraft:ore");
                feature.addProperty("size", ore.size());
                feature.addProperty("discard_chance_on_air_exposure", 0.0);
                feature.add("targets", targets);
                this.pack.putJson("data/almura/worldgen/feature/" + name + ".json", feature);
                final JsonArray placement = new JsonArray();
                final JsonObject count = new JsonObject();
                count.addProperty("type", "minecraft:count");
                count.addProperty("count", ore.count());
                placement.add(count);
                final JsonObject square = new JsonObject();
                square.addProperty("type", "minecraft:in_square");
                placement.add(square);
                final JsonObject height = new JsonObject();
                height.addProperty("type", "minecraft:height_range");
                final JsonObject uniform = new JsonObject();
                uniform.addProperty("type", "minecraft:uniform");
                final JsonObject min = new JsonObject();
                min.addProperty("absolute", 0);
                final JsonObject max = new JsonObject();
                max.addProperty("absolute", 63);
                uniform.add("min_inclusive", min);
                uniform.add("max_inclusive", max);
                height.add("height", uniform);
                placement.add(height);
                final JsonObject biome = new JsonObject();
                biome.addProperty("type", "minecraft:biome");
                placement.add(biome);
                final JsonObject placed = new JsonObject();
                placed.addProperty("feature", "almura:" + name);
                placed.add("placement", placement);
                this.pack.putJson("data/almura/worldgen/placed_feature/" + name + ".json", placed);
                ores.add("almura:" + name);
            }
        }
        if (!ores.isEmpty()) {
            this.biomeModifier("content_ores", ores, "underground_ores");
        }
    }

    /**
     * A code feature that places itself once a chunk, from the chunk's corner.
     */
    private String codeFeature(final String name) {
        final JsonObject feature = new JsonObject();
        feature.addProperty("type", "almura:" + name);
        this.pack.putJson("data/almura/worldgen/feature/" + name + ".json", feature);
        final JsonObject placed = new JsonObject();
        placed.addProperty("feature", "almura:" + name);
        placed.add("placement", new JsonArray());
        this.pack.putJson("data/almura/worldgen/placed_feature/" + name + ".json", placed);
        return "almura:" + name;
    }

    private void biomeModifier(final String name, final List<String> features, final String step) {
        final JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "neoforge:add_features");
        modifier.addProperty("biomes", "#minecraft:is_overworld");
        final JsonArray list = new JsonArray();
        features.forEach(list::add);
        modifier.add("features", list);
        modifier.addProperty("step", step);
        this.pack.putJson("data/almura/neoforge/biome_modifier/" + name + ".json", modifier);
    }

    /**
     * Crops keep the farmland under them ({@code minecraft:maintains_farmland}) and are crops to the game ({@code minecraft:crops}: bees,
     * villagers), as 1.12's {@code BlockCrops} were.
     */
    private void cropTags(final List<BlockDef> blocks) {
        final JsonArray values = new JsonArray();
        blocks.stream().filter(block -> block.kind().equals("crop") && this.crops.containsKey(block.id())).forEach(block -> values.add(block.id()));
        if (values.isEmpty()) {
            return;
        }
        for (final String tag : new String[] {"maintains_farmland", "crops"}) {
            final JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", values.deepCopy());
            this.pack.putJson("data/minecraft/tags/block/" + tag + ".json", json);
        }
    }

    /**
     * A slab drops itself, two when doubled, as vanilla's slabs do.
     */
    private void slabLootTable(final BlockDef block) {
        final JsonObject setCount = new JsonObject();
        setCount.addProperty("type", "minecraft:set_count");
        setCount.add("condition", matchBlock(block.id(), "type", "double"));
        setCount.addProperty("count", 2);
        final JsonObject pool = pool(block.id(), 1, 1, 0);
        final JsonArray modifiers = new JsonArray();
        modifiers.add(setCount);
        pool.getAsJsonArray("entries").get(0).getAsJsonObject().add("modifier", modifiers);
        final JsonArray pools = new JsonArray();
        pools.add(pool);
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace(block.id()) + ":blocks/" + path(block.id()));
        this.pack.putJson("data/" + namespace(block.id()) + "/loot_table/blocks/" + path(block.id()) + ".json", table);
    }

    private static JsonObject pool(final String item, final int min, final int max, final int chancePercent) {
        return pool(item, min, max, chancePercent, null);
    }

    /**
     * A pool of one item, in 26.3's loot format: a single {@code condition} (several joined with {@code minecraft:all_of}) and the
     * item's {@code modifier} list. The item survives explosions as 1.12's drops did, drops at its chance, and only when {@code also} holds.
     */
    private static JsonObject pool(final String item, final int min, final int max, final int chancePercent, final @Nullable JsonObject also) {
        final JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        if (min != 1 || max != 1) {
            final JsonObject count = new JsonObject();
            count.addProperty("type", "minecraft:set_count");
            if (min == max) {
                count.addProperty("count", min);
            } else {
                final JsonObject range = new JsonObject();
                range.addProperty("type", "minecraft:uniform");
                range.addProperty("min", min);
                range.addProperty("max", max);
                count.add("count", range);
            }
            final JsonArray modifiers = new JsonArray();
            modifiers.add(count);
            entry.add("modifier", modifiers);
        }
        final JsonArray entries = new JsonArray();
        entries.add(entry);
        final JsonArray terms = new JsonArray();
        final JsonObject survives = new JsonObject();
        survives.addProperty("type", "minecraft:survives_explosion");
        terms.add(survives);
        if (chancePercent > 0) {
            final JsonObject chance = new JsonObject();
            chance.addProperty("type", "minecraft:random_chance");
            chance.addProperty("chance", chancePercent / 100.0);
            terms.add(chance);
        }
        if (also != null) {
            terms.add(also);
        }
        final JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        if (terms.size() == 1) {
            pool.add("condition", terms.get(0));
        } else {
            final JsonObject all = new JsonObject();
            all.addProperty("type", "minecraft:all_of");
            all.add("terms", terms);
            pool.add("condition", all);
        }
        return pool;
    }

    /**
     * 26.3's block state condition: the block broken is this one, with the property at the value.
     */
    private static JsonObject matchBlock(final String block, final String property, final String value) {
        final JsonObject state = new JsonObject();
        state.addProperty(property, value);
        final JsonObject match = new JsonObject();
        match.addProperty("type", "minecraft:match_block");
        match.addProperty("blocks", block);
        match.add("state", state);
        return match;
    }

    /**
     * {@code mineable/pickaxe} and its kin from the blocks' effective tools, and the tier the cheapest of them needs.
     */
    private void toolTags(final List<BlockDef> blocks) {
        final Map<String, Set<String>> tags = new TreeMap<>();
        for (final BlockDef block : blocks) {
            if (block.isDoubleSlab()) {
                continue;
            }
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

    /**
     * {@code almura:mineable/sickle}: the blocks sickles cut quickly. The crops they name come later, so each is optional.
     */
    private void sickleTag(final List<ItemDef> items) {
        final Set<String> blocks = new TreeSet<>();
        items.stream().filter(item -> item.kind().equals("tool_sickle")).forEach(item -> blocks.addAll(item.effectiveOn()));
        if (blocks.isEmpty()) {
            return;
        }
        final JsonArray values = new JsonArray();
        for (final String block : blocks) {
            final JsonObject value = new JsonObject();
            value.addProperty("id", block);
            value.addProperty("required", false);
            values.add(value);
        }
        final JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        json.add("values", values);
        this.pack.putJson("data/almura/tags/block/mineable/sickle.json", json);
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
