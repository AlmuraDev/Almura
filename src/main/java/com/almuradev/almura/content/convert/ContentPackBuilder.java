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

    public ContentPackBuilder(final ContentSource source) {
        this.source = source;
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
            if (!Identifier.isValidPath(file)) {
                // 26.3 refuses the whole resource reload over one such name (capitals, spaces), so it is left out
                this.problems.add(namespace + ":" + file + ": not a valid 26.3 file name (lower case a-z, 0-9, / . _ - only); left out");
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
     * given {@code block/block}'s display (Forge's {@code forge:default-block}, which every such blockstate names); elements given with
     * {@code from} past {@code to} put the right way round, or 26.3 turns their faces inward (the flags' cloth).
     */
    static JsonObject model(final JsonObject model, final boolean block) {
        final JsonObject copy = model.deepCopy();
        if (block && !copy.has("parent") && copy.has("elements")) {
            copy.addProperty("parent", "minecraft:block/block");
        }
        if (copy.get("elements") instanceof final JsonArray elements) {
            for (final JsonElement element : elements) {
                if (element instanceof final JsonObject object && object.get("from") instanceof final JsonArray from
                    && object.get("to") instanceof final JsonArray to && from.size() == 3 && to.size() == 3) {
                    for (int axis = 0; axis < 3; axis++) {
                        final double a = from.get(axis).getAsDouble(), b = to.get(axis).getAsDouble();
                        if (a > b) {
                            from.set(axis, new JsonPrimitive(b));
                            to.set(axis, new JsonPrimitive(a));
                        }
                    }
                }
            }
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
        // A model with its own elements and no particle texture (the flags) shows the missing texture when broken; 1.12 did too
        if (copy.has("elements") && copy.get("textures") instanceof final JsonObject textures && !textures.has("particle")) {
            textures.keySet().stream().findFirst().ifPresent(first -> textures.add("particle", textures.get(first).deepCopy()));
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
            case "horizontal" -> {
                for (final String facing : FACINGS) {
                    this.addVariant(variants, "facing=" + facing, this.variant(block, forge, Map.of("facing", facing), "facing=" + facing));
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
        // The material library, found as 1.12's Forge found it ("mtllib almura:x.mtl" beside the model); NeoForge would look for
        // assets/almura/x.mtl, so the wrapper names it outright
        final String mtl = this.materialLibrary(models, objPath, folder);
        final JsonObject textures = new JsonObject();
        String particle = null;
        for (final String map : this.materialTextures(mtl == null ? models.resolve(base + ".mtl") : models.resolve(mtl))) {
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
        // As 3.5's OBJ reader did: the particles show the part of the sheet the model's last face uses, not the whole sheet
        final String cut = this.objParticle(namespace, folder, objPath, base, mtl == null ? models.resolve(base + ".mtl") : models.resolve(mtl));
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
        // 3.5's OBJ reader flipped V (v = 1 - v), as the content was made for; NeoForge only does that when asked
        wrapper.addProperty("flip_v", true);
        if (mtl != null) {
            wrapper.addProperty("mtl_override", namespace + ":models/" + mtl);
        } else {
            this.problems.add(id + ": no material library found for models/" + objPath);
        }
        wrapper.add("textures", textures);
        this.pack.putJson(file, wrapper);
        return id;
    }

    /**
     * The OBJ's material library ({@code mtllib}), as 1.12's Forge read it: a name without a folder is beside the model, one with a folder
     * is under {@code models/block/} or {@code models/}; failing those, the model's own name with {@code .mtl}.
     *
     * @return The library's path under {@code models/}, or null if there's none
     */
    private @Nullable String materialLibrary(final Path models, final String objPath, final String folder) {
        String named = null;
        try {
            for (final String line : Files.readAllLines(models.resolve(objPath), StandardCharsets.UTF_8)) {
                final String trimmed = line.trim();
                if (trimmed.startsWith("mtllib ")) {
                    named = trimmed.substring("mtllib ".length()).trim();
                    break;
                }
            }
        } catch (final IOException e) {
            this.problems.add(objPath + ": couldn't read: " + e.getMessage());
        }
        final List<String> candidates = new ArrayList<>();
        if (named != null) {
            String path = named.contains(":") ? named.substring(named.indexOf(':') + 1) : named;
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            final String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
            candidates.add(folder + name);
            candidates.add("block/" + path);
            candidates.add(path);
        }
        candidates.add(objPath.substring(0, objPath.length() - ".obj".length()) + ".mtl");
        for (final String candidate : candidates) {
            if (Files.isRegularFile(models.resolve(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * The OBJ's particle texture, as 3.5's OBJ reader made it: the part of its texture sheet between the UVs of the first and third corners
     * of its last face, here cut out and scaled to 16x16 ({@code textures/block/obj_particle/...}).
     *
     * @return The texture's id, or null if the face or its texture can't be found
     */
    private @Nullable String objParticle(final String namespace, final String folder, final String objPath, final String base,
        final Path mtl) {
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
        final String map = this.materialMap(mtl, faceMaterial);
        final String texture = map == null ? null : this.textureOf(namespace, folder, map);
        if (first == null || third == null || texture == null) {
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
     * Cuts the part of an image between two UVs (V flipped, as the OBJs were made for) and scales it to 16x16, a size that keeps the block
     * atlas's mipmaps; an animated texture's first frame is used.
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
            final int y1 = clamp(Math.round(Math.min(1 - a[1], 1 - b[1]) * height), height - 1);
            final int y2 = clamp(Math.round(Math.max(1 - a[1], 1 - b[1]) * height), height);
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
     * A block item's model definition: its own item model if the content has one ({@code models/item/<path>.json}, as slabs do), else the
     * model of the block's default variant (north facing, upright, bottom, or the first).
     */
    private void blockItemModel(final BlockDef block) {
        if (block.isDoubleSlab()) {
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
     * doesn't exist (a typo in the content; 1.12 dropped nothing) is left out, the block dropping itself if nothing is left.
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
        final JsonArray pools = new JsonArray();
        List<DropDef> drops = new ArrayList<>();
        for (final DropDef drop : block.state().drops()) {
            if (knownItem.test(drop.item())) {
                drops.add(drop);
            } else {
                this.problems.add(block.id() + ": drops " + drop.item() + ", which doesn't exist; it drops itself instead");
            }
        }
        if (drops.isEmpty()) {
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
     * A slab drops itself, two when doubled, as vanilla's slabs do.
     */
    private void slabLootTable(final BlockDef block) {
        final JsonObject doubled = new JsonObject();
        doubled.addProperty("block", block.id());
        final JsonObject type = new JsonObject();
        type.addProperty("type", "double");
        doubled.add("properties", type);
        doubled.addProperty("condition", "minecraft:block_state_property");
        final JsonArray conditions = new JsonArray();
        conditions.add(doubled);
        final JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.addProperty("count", 2);
        setCount.add("conditions", conditions);
        final JsonObject pool = pool(block.id(), 1, 1, 0);
        final JsonArray functions = new JsonArray();
        functions.add(setCount);
        pool.getAsJsonArray("entries").get(0).getAsJsonObject().add("functions", functions);
        final JsonArray pools = new JsonArray();
        pools.add(pool);
        final JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", namespace(block.id()) + ":blocks/" + path(block.id()));
        this.pack.putJson("data/" + namespace(block.id()) + "/loot_table/blocks/" + path(block.id()) + ".json", table);
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
                    // Content ids are lower case; 1.12 matched "crop_diamond_Sickle.name" to them all the same
                    final String id = (file.folder() + name).replace('/', '.').toLowerCase(java.util.Locale.ROOT);
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
