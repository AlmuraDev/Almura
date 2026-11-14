/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.def;

import com.almuradev.almura.content.def.ContentDefs.BiomeRange;
import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.DecayDef;
import com.almuradev.almura.content.def.ContentDefs.GrassDef;
import com.almuradev.almura.content.def.ContentDefs.GrassEntryDef;
import com.almuradev.almura.content.def.ContentDefs.GrassGenDef;
import com.almuradev.almura.content.def.ContentDefs.OreDef;
import com.almuradev.almura.content.def.ContentDefs.OreGenDef;
import com.almuradev.almura.content.def.ContentDefs.StateRef;
import com.almuradev.almura.content.def.ContentDefs.TreeGenDef;
import com.almuradev.almura.content.def.ContentDefs.LeafDef;
import com.almuradev.almura.content.def.ContentDefs.SaplingDef;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import com.almuradev.almura.content.def.ContentDefs.TreeHeightDef;
import com.almuradev.almura.content.def.ContentDefs.CropDef;
import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationBlockDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.def.ContentDefs.SeedDef;
import com.almuradev.almura.content.def.ContentDefs.Box;
import com.almuradev.almura.content.def.ContentDefs.DropDef;
import com.almuradev.almura.content.def.ContentDefs.EffectDef;
import com.almuradev.almura.content.def.ContentDefs.FoodDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.def.ContentDefs.StateDef;
import com.almuradev.almura.content.source.ContentSource;
import com.almuradev.almura.content.source.ContentSource.ContentFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads the content files' 1.12 JSON into {@link ContentDefs}, keeping the meanings 1.12's processors gave each key (see
 * {@code legacy/.../content/type}). A file that can't be read is left out with a problem noted in the source.
 */
public final class ContentParser {

    public static final String DEFAULT_STATE = "normal", NORTH = "facing=north";

    private final ContentSource source;

    public ContentParser(final ContentSource source) {
        this.source = source;
    }

    public List<ItemGroupDef> itemGroups() {
        final List<ItemGroupDef> groups = new ArrayList<>();
        for (final ContentFile file : this.source.files("item_group")) {
            groups.add(new ItemGroupDef(file.id(), string(file.json(), "icon")));
        }
        return groups;
    }

    /**
     * @param kinds the item kinds to read ({@code normal}, {@code food}); the rest are left for later
     */
    public List<ItemDef> items(final Set<String> kinds) {
        final List<ItemDef> items = new ArrayList<>();
        for (final ContentFile file : this.source.files("item")) {
            final String kind = kind(file);
            if (!kinds.contains(kind)) {
                continue;
            }
            final JsonObject json = file.json();
            String container = null;
            int containerDamage = 0;
            final JsonElement containerElement = json.get("container");
            if (containerElement != null && containerElement.isJsonPrimitive()) {
                container = containerElement.getAsString();
            } else if (containerElement != null && containerElement.isJsonObject()) {
                container = string(containerElement.getAsJsonObject(), "item");
                containerDamage = integer(containerElement.getAsJsonObject(), "damage", 0);
            }
            items.add(new ItemDef(file.id(), kind, string(json, "item_group"), integer(json, "max_stack_size", 0), integer(json, "durability", 0),
                container, containerDamage, json.has("apply") ? this.food(json) : null, string(json, "tier"), strings(json.get("effective_on"))));
        }
        return items;
    }

    private FoodDef food(final JsonObject json) {
        final JsonObject apply = json.getAsJsonObject("apply");
        final int[] nutrition = range(apply.get("food_level_change"), 0);
        final List<EffectDef> effects = new ArrayList<>();
        final JsonElement potions = json.get("potion_effects");
        if (potions != null && potions.isJsonArray()) {
            for (final JsonElement element : potions.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                final JsonObject effect = element.getAsJsonObject();
                final int[] chance = range(effect.get("chance"), 100);
                effects.add(new EffectDef(strings(effect.get("potion")), chance[0], chance[1], integer(effect, "duration", 0),
                    integer(effect, "amplifier", 0), bool(effect, "ambient", false), bool(effect, "show_particles", true)));
            }
        }
        return new FoodDef(nutrition[0], nutrition[1], (float) number(apply, "saturation_change", 0), (float) number(apply, "health_change", 0),
            integer(json, "duration_ticks", 32), bool(json, "always_edible", false), effects);
    }

    /**
     * @param kinds the block kinds to read ({@code normal}, {@code horizontal}); the rest are left for later
     */
    public List<BlockDef> blocks(final Set<String> kinds) {
        final List<BlockDef> blocks = new ArrayList<>();
        for (final ContentFile file : this.source.files("block")) {
            final String kind = kind(file);
            if (!kinds.contains(kind)) {
                continue;
            }
            final JsonObject json = file.json();
            final JsonObject state = this.defaultState(file);
            if (state == null) {
                continue;
            }
            blocks.add(new BlockDef(file.id(), kind, stringOr(json, "material", "minecraft:rock"), string(json, "map_color"), string(json, "sound"),
                string(json, "item_group"), strings(json.get("effective_tools")), string(json, "render_layer"), this.state(state),
                string(json, "block"), string(json, "single")));
        }
        return blocks;
    }

    /**
     * The leaves ({@code content/block/leaf}): their fruit spread and decay drops.
     */
    public List<LeafDef> leaves() {
        final List<LeafDef> leaves = new ArrayList<>();
        for (final ContentFile file : this.source.files("block")) {
            if (!kind(file).equals("leaf")) {
                continue;
            }
            final JsonObject state = this.resolveState(file, ContentParser.DEFAULT_STATE);
            if (state == null) {
                continue;
            }
            final JsonObject spread = object(state, "spread");
            final List<DecayDef> decay = new ArrayList<>();
            final JsonObject action = object(state, "action");
            if (action != null && action.get("decay") instanceof final JsonArray decays) {
                for (final JsonElement element : decays) {
                    if (element instanceof final JsonObject entry) {
                        decay.add(new DecayDef(biomeRanges(entry.get("chance")), drops(entry.get("drop"))));
                    }
                }
            }
            leaves.add(new LeafDef(file.id(), spread == null ? null : string(spread, "block"),
                spread == null ? List.of() : biomeRanges(spread.get("chance")), decay));
        }
        return leaves;
    }

    /**
     * The saplings ({@code content/block/sapling}) and the trees they grow.
     */
    public List<SaplingDef> saplings() {
        final List<SaplingDef> saplings = new ArrayList<>();
        for (final ContentFile file : this.source.files("block")) {
            if (!kind(file).equals("sapling")) {
                continue;
            }
            final JsonObject state = this.resolveState(file, ContentParser.DEFAULT_STATE);
            final String tree = state == null ? null : string(state, "tree");
            if (tree == null) {
                this.source.problem(file.file(), "Sapling names no tree");
                continue;
            }
            saplings.add(new SaplingDef(file.id(), tree));
        }
        return saplings;
    }

    /**
     * The trees ({@code content/tree}).
     */
    public List<TreeDef> trees() {
        final List<TreeDef> trees = new ArrayList<>();
        for (final ContentFile file : this.source.files("tree")) {
            final JsonObject json = file.json();
            final String log = string(json, "log"), leaves = string(json, "leaves");
            if (log == null || leaves == null) {
                this.source.problem(file.file(), "Tree names no log or leaves");
                continue;
            }
            final List<TreeHeightDef> heights = new ArrayList<>();
            final JsonElement height = json.get("height");
            for (final JsonElement element : height instanceof final JsonArray array ? array : height == null ? new JsonArray() : asArray(height)) {
                if (element instanceof final JsonObject entry) {
                    final JsonObject variance = object(entry, "variance");
                    heights.add(new TreeHeightDef(strings(entry.get("biome")), integer(entry, "min", 4),
                        variance == null ? 0 : integer(variance, "min", 0), variance == null ? 0 : integer(variance, "max", 0)));
                }
            }
            if (heights.isEmpty()) {
                heights.add(new TreeHeightDef(List.of(), 4, 0, 0));
            }
            final JsonObject fruit = object(json, "fruit");
            final String shape = bool(json, "palm", false) ? "palm" : bool(json, "big", false) ? "big" : "normal";
            trees.add(new TreeDef(file.id(), heights, log, leaves, fruit == null ? null : string(fruit, "block"),
                fruit == null ? List.of() : biomeRanges(fruit.get("chance")), shape));
        }
        return trees;
    }

    /**
     * The trees in world generation ({@code content/generation/feature/tree}), in the content's order.
     */
    public List<TreeGenDef> treeGeneration() {
        final List<TreeGenDef> trees = new ArrayList<>();
        for (final ContentFile file : this.source.files("generation")) {
            if (!file.path().startsWith("feature/tree/")) {
                continue;
            }
            final String tree = string(file.json(), "tree");
            if (tree == null) {
                this.source.problem(file.file(), "Tree generation names no tree");
                continue;
            }
            trees.add(new TreeGenDef(file.id(), tree, legacyBiomes(biomeRanges(file.json().get("chance"))), legacyBlocks(strings(file.json().get("requires")))));
        }
        return trees;
    }

    /**
     * The grass mixes in world generation ({@code content/generation/feature/grass}).
     */
    public List<GrassGenDef> grassGeneration() {
        final List<GrassGenDef> grasses = new ArrayList<>();
        for (final ContentFile file : this.source.files("generation")) {
            if (!file.path().startsWith("feature/grass/")) {
                continue;
            }
            final String grass = string(file.json(), "grass");
            if (grass == null) {
                this.source.problem(file.file(), "Grass generation names no grass");
                continue;
            }
            grasses.add(new GrassGenDef(file.id(), grass, legacyBiomes(biomeRanges(file.json().get("chance"))),
                legacyBlocks(strings(file.json().get("requires")))));
        }
        return grasses;
    }

    /**
     * The grass mixes ({@code content/grass}).
     */
    public List<GrassDef> grasses() {
        final List<GrassDef> grasses = new ArrayList<>();
        for (final ContentFile file : this.source.files("grass")) {
            final List<GrassEntryDef> entries = new ArrayList<>();
            if (file.json().get("grasses") instanceof final JsonArray array) {
                for (final JsonElement element : array) {
                    if (!(element instanceof final JsonObject entry)) {
                        continue;
                    }
                    final List<StateRef> blocks = new ArrayList<>();
                    final JsonElement block = entry.get("block");
                    for (final JsonElement one : block instanceof final JsonArray list ? list : block == null ? new JsonArray() : asArray(block)) {
                        final StateRef state = stateRef(one);
                        if (state != null) {
                            blocks.add(state);
                        }
                    }
                    if (!blocks.isEmpty()) {
                        entries.add(new GrassEntryDef(integer(entry, "weight", 1), blocks));
                    }
                }
            }
            grasses.add(new GrassDef(file.id(), entries));
        }
        return grasses;
    }

    /**
     * The ores in world generation ({@code content/generation/underground/ore/<dimension>.json}).
     */
    public List<OreGenDef> oreGeneration() {
        final List<OreGenDef> ores = new ArrayList<>();
        for (final ContentFile file : this.source.files("generation")) {
            if (!file.path().startsWith("underground/ore/")) {
                continue;
            }
            final List<OreDef> entries = new ArrayList<>();
            if (file.json().get("entries") instanceof final JsonArray array) {
                for (final JsonElement element : array) {
                    if (element instanceof final JsonObject entry && string(entry, "block") != null) {
                        entries.add(new OreDef(string(entry, "block"), integer(entry, "size", 8), integer(entry, "count", 1)));
                    }
                }
            }
            ores.add(new OreGenDef(file.path().substring("underground/ore/".length()), entries));
        }
        return ores;
    }

    /**
     * A block state: {@code "almura:flower/amaryllis"} or {@code {"id": "minecraft:tallgrass", "state": {"type": "tall_grass"}}}, in 26.3's
     * names (1.12's tall grass is 26.3's short grass, its double grass 26.3's tall grass).
     */
    static @Nullable StateRef stateRef(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new StateRef(element.getAsString(), Map.of());
        }
        if (!(element instanceof final JsonObject object) || string(object, "id") == null) {
            return null;
        }
        final Map<String, String> properties = new LinkedHashMap<>();
        final JsonObject state = object(object, "state");
        if (state != null) {
            state.entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue().getAsString()));
        }
        final String id = string(object, "id");
        if (id.equals("minecraft:tallgrass")) {
            return new StateRef("fern".equals(properties.get("type")) ? "minecraft:fern" : "minecraft:short_grass", Map.of());
        }
        if (id.equals("minecraft:double_plant")) {
            final String variant = properties.getOrDefault("variant", "double_grass");
            return new StateRef(switch (variant) {
                case "double_fern" -> "minecraft:large_fern";
                case "sunflower" -> "minecraft:sunflower";
                case "syringa" -> "minecraft:lilac";
                case "double_rose" -> "minecraft:rose_bush";
                case "paeonia" -> "minecraft:peony";
                default -> "minecraft:tall_grass";
            }, Map.of());
        }
        return new StateRef(id, properties);
    }

    private static List<BiomeRange> legacyBiomes(final List<BiomeRange> ranges) {
        final List<BiomeRange> mapped = new ArrayList<>();
        for (final BiomeRange range : ranges) {
            final Set<String> biomes = new java.util.LinkedHashSet<>();
            range.biomes().forEach(biome -> biomes.add(legacyBiome(biome)));
            mapped.add(new BiomeRange(List.copyOf(biomes), range.range()));
        }
        return mapped;
    }

    /**
     * 1.12's biome ids as 26.3's: renamed ones renamed, ones folded into others (the hills, the "mutated" variants) as what they became.
     */
    static String legacyBiome(final String biome) {
        return switch (biome) {
            case "minecraft:swampland" -> "minecraft:swamp";
            case "minecraft:roofed_forest" -> "minecraft:dark_forest";
            case "minecraft:beaches" -> "minecraft:beach";
            case "minecraft:mesa" -> "minecraft:badlands";
            case "minecraft:mesa_rock" -> "minecraft:wooded_badlands";
            case "minecraft:savanna_rock" -> "minecraft:savanna_plateau";
            case "minecraft:jungle_edge", "minecraft:mutated_jungle_edge" -> "minecraft:sparse_jungle";
            case "minecraft:forest_hills" -> "minecraft:forest";
            case "minecraft:jungle_hills", "minecraft:mutated_jungle" -> "minecraft:jungle";
            case "minecraft:birch_forest_hills" -> "minecraft:birch_forest";
            case "minecraft:taiga_hills" -> "minecraft:taiga";
            case "minecraft:desert_hills" -> "minecraft:desert";
            case "minecraft:extreme_hills" -> "minecraft:windswept_hills";
            case "minecraft:ice_flats" -> "minecraft:snowy_plains";
            case "minecraft:mushroom_island", "minecraft:mushroom_island_shore" -> "minecraft:mushroom_fields";
            case "minecraft:stone_beach" -> "minecraft:stony_shore";
            case "minecraft:cold_beach" -> "minecraft:snowy_beach";
            case "minecraft:mutated_forest" -> "minecraft:flower_forest";
            case "minecraft:mutated_plains" -> "minecraft:sunflower_plains";
            default -> biome;
        };
    }

    private static List<String> legacyBlocks(final List<String> blocks) {
        return blocks.stream().map(block -> switch (block) {
            case "minecraft:grass" -> "minecraft:grass_block";
            default -> block;
        }).toList();
    }

    private static JsonArray asArray(final JsonElement element) {
        final JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }

    /**
     * 1.12's chance list ({@code [{"biome": ["minecraft"], "min": 0, "max": 10}, {"min": 0, "max": 10}]}), or a lone range.
     */
    static List<BiomeRange> biomeRanges(final @Nullable JsonElement element) {
        final List<BiomeRange> ranges = new ArrayList<>();
        if (element == null) {
            return ranges;
        }
        for (final JsonElement entry : element instanceof final JsonArray array ? array : asArray(element)) {
            if (entry instanceof final JsonObject object) {
                final double min = number(object, "min", 0);
                ranges.add(new BiomeRange(strings(object.get("biome")), new Range(min, number(object, "max", min))));
            } else if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) {
                ranges.add(new BiomeRange(List.of(), new Range(entry.getAsDouble(), entry.getAsDouble())));
            }
        }
        return ranges;
    }

    /**
     * Drops ({@code [{"item": ..., "amount": 1, "bonus": {"amount": 1, "chance": 10}}]}).
     */
    static List<DropDef> drops(final @Nullable JsonElement element) {
        final List<DropDef> drops = new ArrayList<>();
        if (element instanceof final JsonArray array) {
            for (final JsonElement drop : array) {
                if (drop instanceof final JsonObject dropJson && string(dropJson, "item") != null) {
                    final int[] amount = range(dropJson.get("amount"), 1);
                    final JsonObject bonus = object(dropJson, "bonus");
                    drops.add(new DropDef(string(dropJson, "item"), amount[0], amount[1], bonus == null ? 0 : integer(bonus, "amount", 0),
                        bonus == null ? 0 : integer(bonus, "chance", 0)));
                }
            }
        }
        return drops;
    }

    /**
     * The seeds ({@code content/item/seed}).
     */
    public List<SeedDef> seeds() {
        final List<SeedDef> seeds = new ArrayList<>();
        for (final ContentFile file : this.source.files("item")) {
            if (!kind(file).equals("seed")) {
                continue;
            }
            final JsonObject json = file.json();
            final String crop = string(json, "crop");
            if (crop == null) {
                this.source.problem(file.file(), "Seed names no crop");
                continue;
            }
            final JsonObject grass = object(json, "grass");
            final Range chance = grass == null ? null : firstRange(grass.get("chance"));
            final Range amount = grass == null ? null : firstRange(grass.get("amount"));
            seeds.add(new SeedDef(file.id(), crop, stringOr(json, "soil", "minecraft:farmland"), chance,
                amount == null ? 1 : (int) amount.min(), amount == null ? 1 : (int) amount.max()));
        }
        return seeds;
    }

    /**
     * The crops ({@code content/block/crop}), each with its stages; a crop whose seed isn't among the seeds is planted on farmland.
     */
    public List<CropDef> crops(final List<SeedDef> seeds) {
        final List<CropDef> crops = new ArrayList<>();
        for (final ContentFile file : this.source.files("block")) {
            if (!kind(file).equals("crop")) {
                continue;
            }
            final JsonObject json = file.json();
            final JsonObject states = object(json, "state");
            final List<CropStageDef> stages = new ArrayList<>();
            for (int age = 0; states != null && states.has("age=" + age); age++) {
                final JsonObject stage = this.resolveState(file, "age=" + age);
                if (stage == null) {
                    break;
                }
                stages.add(this.cropStage(file, age, stage));
            }
            if (stages.size() < 2) {
                this.source.problem(file.file(), "Crop has fewer than two stages (age=0, age=1...)");
                continue;
            }
            final String seed = stringOr(json, "seed", "");
            final String soil = seeds.stream().filter(def -> def.id().equals(seed)).map(SeedDef::soil).findFirst().orElse("minecraft:farmland");
            final JsonObject sack = object(json, "sack_harvest");
            final JsonElement aquatic = json.get("aquatic");
            // "aquatic": true was the first spelling, a submerged crop
            final String aquaticMode = aquatic == null || !aquatic.isJsonPrimitive() ? null
                : aquatic.getAsJsonPrimitive().isBoolean() ? (aquatic.getAsBoolean() ? "submerged" : null) : aquatic.getAsString();
            crops.add(new CropDef(file.id(), seed, soil, stages, sack == null ? -1 : integer(sack, "reset_age", -1), aquaticMode));
        }
        return crops;
    }

    private CropStageDef cropStage(final ContentFile file, final int age, final JsonObject json) {
        final JsonObject growth = object(json, "growth");
        HydrationDef hydration = null;
        final JsonObject hydrationJson = object(json, "hydration");
        if (hydrationJson != null && hydrationJson.get("block") instanceof final JsonArray blocks) {
            final List<HydrationBlockDef> matches = new ArrayList<>();
            for (final JsonElement element : blocks) {
                if (!(element instanceof final JsonObject block) || string(block, "id") == null) {
                    continue;
                }
                final Map<String, Range> properties = new LinkedHashMap<>();
                final JsonObject state = object(block, "state");
                if (state != null) {
                    for (final Map.Entry<String, JsonElement> property : state.entrySet()) {
                        if (property.getValue() instanceof final JsonObject range) {
                            properties.put(property.getKey(), new Range(number(range, "min", 0), number(range, "max", number(range, "min", 0))));
                        } else if (property.getValue().isJsonPrimitive() && property.getValue().getAsJsonPrimitive().isNumber()) {
                            properties.put(property.getKey(), new Range(property.getValue().getAsDouble(), property.getValue().getAsDouble()));
                        }
                    }
                }
                matches.add(new HydrationBlockDef(string(block, "id"), properties));
            }
            hydration = new HydrationDef(matches, integer(hydrationJson, "max_radius", 0));
        }
        Range bonemeal = null;
        if (json.get("fertilizer") instanceof final JsonArray fertilizers) {
            for (final JsonElement element : fertilizers) {
                if (element instanceof final JsonObject fertilizer && isBoneMeal(fertilizer.get("item"))) {
                    bonemeal = firstRange(fertilizer.get("chance"));
                } else {
                    this.source.problem(file.file(), "Only bone meal (minecraft:dye 15) is read as a fertilizer: " + element);
                }
            }
        }
        return new CropStageDef(age, this.state(json), growth == null ? null : firstRange(growth.get("chance")),
            growth == null ? null : firstRange(growth.get("light")), growth == null ? null : firstRange(growth.get("temperature_required")),
            hydration, bonemeal, bool(json, "can_rollback", false));
    }

    /**
     * 1.12's bone meal: {@code {"minecraft:dye": {"meta": 15}}}, or 26.3's {@code minecraft:bone_meal}.
     */
    private static boolean isBoneMeal(final @Nullable JsonElement item) {
        if (item instanceof final JsonObject object && object.get("minecraft:dye") instanceof final JsonObject dye) {
            return integer(dye, "meta", 0) == 15;
        }
        return item != null && item.isJsonPrimitive() && item.getAsString().equals("minecraft:bone_meal");
    }

    /**
     * The first of 1.12's per-biome ranges ({@code [{"min": 5, "max": 10}]}; the content gives only the default one), or a lone range or
     * number.
     */
    static @Nullable Range firstRange(final @Nullable JsonElement element) {
        JsonElement first = element;
        if (element instanceof final JsonArray array) {
            first = array.isEmpty() ? null : array.get(0);
        }
        if (first instanceof final JsonObject object) {
            final double min = number(object, "min", 0);
            return new Range(min, number(object, "max", min));
        }
        if (first != null && first.isJsonPrimitive() && first.getAsJsonPrimitive().isNumber()) {
            return new Range(first.getAsDouble(), first.getAsDouble());
        }
        return null;
    }

    /**
     * The state that defines the block, with what it inherits from its parents ({@code parent}: another state's name) filled in.
     */
    private @Nullable JsonObject defaultState(final ContentFile file) {
        final JsonObject states = file.json().getAsJsonObject("state");
        if (states == null || states.isEmpty()) {
            return new JsonObject();
        }
        final String name = states.has(DEFAULT_STATE) ? DEFAULT_STATE : states.has(NORTH) ? NORTH : states.has("age=0") ? "age=0"
            : states.keySet().iterator().next();
        return this.resolveState(file, name);
    }

    /**
     * A state with what it inherits from its parents ({@code parent}: another state's name) filled in.
     */
    private @Nullable JsonObject resolveState(final ContentFile file, final String name) {
        final JsonObject states = file.json().getAsJsonObject("state");
        final JsonObject resolved = new JsonObject();
        final Set<String> seen = new HashSet<>();
        String current = name;
        while (current != null && seen.add(current)) {
            final JsonElement element = states.get(current);
            if (element == null || !element.isJsonObject()) {
                this.source.problem(file.file(), "State '" + current + "' is missing");
                break;
            }
            for (final Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (!entry.getKey().equals("parent") && !resolved.has(entry.getKey())) {
                    resolved.add(entry.getKey(), entry.getValue());
                }
            }
            current = string(element.getAsJsonObject(), "parent");
        }
        return resolved;
    }

    private StateDef state(final JsonObject json) {
        Box shape = null, collision = null;
        boolean noCollision = false;
        final JsonObject aabb = object(json, "aabb");
        if (aabb != null) {
            shape = box(aabb.has("shape") ? object(aabb, "shape") : aabb);
            collision = box(object(aabb, "collision"));
            noCollision = aabb.get("collision") != null && aabb.get("collision").isJsonPrimitive() && number(aabb, "collision", 0) < 0;
        }
        final JsonObject light = object(json, "light");
        final List<DropDef> drops = new ArrayList<>();
        final JsonObject action = object(json, "action");
        if (action != null && action.get("destroy") != null && action.get("destroy").isJsonArray()) {
            for (final JsonElement destroy : action.getAsJsonArray("destroy")) {
                if (destroy.isJsonObject() && destroy.getAsJsonObject().get("drop") instanceof final JsonArray dropArray) {
                    for (final JsonElement drop : dropArray) {
                        if (drop.isJsonObject() && string(drop.getAsJsonObject(), "item") != null) {
                            final JsonObject dropJson = drop.getAsJsonObject();
                            final int[] amount = range(dropJson.get("amount"), 1);
                            final JsonObject bonus = object(dropJson, "bonus");
                            drops.add(new DropDef(string(dropJson, "item"), amount[0], amount[1], bonus == null ? 0 : integer(bonus, "amount", 0),
                                bonus == null ? 0 : integer(bonus, "chance", 0)));
                        }
                    }
                }
            }
        }
        return new StateDef((float) number(json, "hardness", Double.NaN), (float) number(json, "resistance", Double.NaN),
            json.has("opaque") ? bool(json, "opaque", true) : null, light == null ? 0f : (float) number(light, "emission", 0), shape, collision,
            noCollision, string(json, "block_face_shape"), drops);
    }

    private static @Nullable Box box(final @Nullable JsonObject json) {
        if (json == null || !(json.get("min") instanceof final JsonArray min) || !(json.get("max") instanceof final JsonArray max)
            || min.size() < 3 || max.size() < 3) {
            return null;
        }
        return new Box(min.get(0).getAsDouble(), min.get(1).getAsDouble(), min.get(2).getAsDouble(), max.get(0).getAsDouble(),
            max.get(1).getAsDouble(), max.get(2).getAsDouble());
    }

    /**
     * The type folder below {@code item} or {@code block}: the first part of the id's path.
     */
    static String kind(final ContentFile file) {
        final int slash = file.path().indexOf('/');
        return slash < 0 ? "" : file.path().substring(0, slash);
    }

    /**
     * A number, or a {@code {min, max}} range, as {@code [min, max]}.
     */
    static int[] range(final @Nullable JsonElement element, final int otherwise) {
        if (element == null || element.isJsonNull()) {
            return new int[] {otherwise, otherwise};
        }
        if (element.isJsonPrimitive()) {
            final int value = (int) Math.round(element.getAsDouble());
            return new int[] {value, value};
        }
        final JsonObject object = element.getAsJsonObject();
        final int min = (int) Math.round(number(object, "min", otherwise));
        return new int[] {min, (int) Math.round(number(object, "max", min))};
    }

    static List<String> strings(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        if (element.isJsonPrimitive()) {
            return List.of(element.getAsString());
        }
        final List<String> strings = new ArrayList<>();
        if (element.isJsonArray()) {
            for (final JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    strings.add(item.getAsString());
                }
            }
        }
        return strings;
    }

    static @Nullable JsonObject object(final JsonObject json, final String key) {
        final JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    static @Nullable String string(final JsonObject json, final String key) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    static String stringOr(final JsonObject json, final String key, final String otherwise) {
        final String value = string(json, key);
        return value == null ? otherwise : value;
    }

    static double number(final JsonObject json, final String key, final double otherwise) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber() ? element.getAsDouble() : otherwise;
    }

    static int integer(final JsonObject json, final String key, final int otherwise) {
        return (int) Math.round(number(json, key, otherwise));
    }

    static boolean bool(final JsonObject json, final String key, final boolean otherwise) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : otherwise;
    }
}
