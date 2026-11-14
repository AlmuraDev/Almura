/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.def;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * What the content files describe, read from their 1.12 JSON (see {@link ContentParser}), before anything is registered. Ids are
 * {@code namespace:path} strings; nothing here uses Minecraft's classes.
 */
public final class ContentDefs {

    private ContentDefs() {
    }

    /**
     * A creative tab: its id ({@code almura:food}) and the item shown on it.
     */
    public record ItemGroupDef(String id, @Nullable String icon) {
    }

    /**
     * An item.
     *
     * @param kind the type folder below {@code item} ({@code normal}, {@code food}, ...)
     * @param group the creative tab's id, if any
     * @param maxStackSize 0 for the default
     * @param durability 0 for none
     * @param container the item given back when it's used up (a bowl), if any
     * @param containerDamage damage done to the item instead, when it has durability ({@code container: {damage: 1}})
     * @param food what eating it does, for food
     * @param tier for a tool, its material ({@code minecraft:iron})
     * @param effectiveOn for a sickle, the blocks it cuts quickly
     */
    public record ItemDef(String id, String kind, @Nullable String group, int maxStackSize, int durability, @Nullable String container,
        int containerDamage, @Nullable FoodDef food, @Nullable String tier, List<String> effectiveOn) {
    }

    /**
     * What eating does: hunger and saturation restored (1.12's {@code apply}), health healed, how long eating takes, whether it can be eaten
     * when full, and effects with their chance.
     */
    public record FoodDef(int nutritionMin, int nutritionMax, float saturation, float health, int durationTicks, boolean alwaysEdible,
        List<EffectDef> effects) {
    }

    /**
     * A potion effect eating may give: the effects' ids, the chance (percent, picked between min and max), how long, how strong, and how it
     * shows.
     */
    public record EffectDef(List<String> effects, int chanceMin, int chanceMax, int durationTicks, int amplifier, boolean ambient,
        boolean showParticles) {
    }

    /**
     * A block.
     *
     * @param kind the type folder below {@code block} ({@code normal}, {@code horizontal}, ...)
     * @param material 1.12's material ({@code minecraft:rock})
     * @param mapColor 1.12's map colour ({@code minecraft:stone})
     * @param sound 1.12's sound group ({@code minecraft:stone}), if set
     * @param effectiveTools the tools that break it quickly
     * @param renderLayer 1.12's layer ({@code cutout_mipped}, {@code translucent}), if set
     * @param state the state that defines it (the default one: {@code normal}, or {@code facing=north} with the other facings its
     *     children)
     * @param base for a stair, the block it is made of ({@code block})
     * @param single for a slab, the single slab ({@code single}); a slab is double when this isn't its own id
     */
    public record BlockDef(String id, String kind, String material, @Nullable String mapColor, @Nullable String sound, @Nullable String group,
        List<String> effectiveTools, @Nullable String renderLayer, StateDef state, @Nullable String base, @Nullable String single) {

        public boolean isDoubleSlab() {
            return this.single != null && !this.single.equals(this.id);
        }
    }

    /**
     * A block state's settings.
     *
     * @param hardness negative for unbreakable, {@link Float#NaN} for the material's
     * @param resistance {@link Float#NaN} for the material's
     * @param opaque null for the material's
     * @param lightEmission 0 to 1
     * @param shape the outline and collision box (0 to 1), north facing, if not a full block
     * @param collision a different collision box, if set
     * @param noCollision whether things pass through it (1.12's {@code "collision": -1})
     * @param drops what breaking it drops; empty for itself
     */
    public record StateDef(float hardness, float resistance, @Nullable Boolean opaque, float lightEmission, @Nullable Box shape,
        @Nullable Box collision, boolean noCollision, @Nullable String blockFaceShape, List<DropDef> drops) {
    }

    public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

        public boolean isFullBlock() {
            return this.minX <= 0 && this.minY <= 0 && this.minZ <= 0 && this.maxX >= 1 && this.maxY >= 1 && this.maxZ >= 1;
        }
    }

    /**
     * A drop: the item and how many (between min and max), and a chance (percent) of some more.
     */
    public record DropDef(String item, int amountMin, int amountMax, int bonusAmount, int bonusChance) {
    }

    /**
     * A range of numbers (1.12's {@code {"min": 5, "max": 10}}); a value is drawn evenly from all of it, as 3.5's {@code DoubleRanges.random}
     * did.
     */
    public record Range(double min, double max) {

        public boolean contains(final double value) {
            return value >= this.min && value <= this.max;
        }

        public double random(final double unit) {
            return this.min + unit * (this.max - this.min);
        }
    }

    /**
     * A crop ({@code content/block/crop}): its seed, the soil its seed plants it on, and its stages ({@code age=0} up).
     *
     * @param soil the block its seed plants it on ({@code minecraft:farmland})
     * @param sackResetAge the age a ripe crop is set back to when picked with a harvesting sack, -1 when it can't be
     * @param aquatic {@code surface} or {@code submerged} for a crop grown in water, else null
     */
    public record CropDef(String id, String seed, String soil, List<CropStageDef> stages, int sackResetAge, @Nullable String aquatic) {

        public int maxAge() {
            return this.stages.size() - 1;
        }
    }

    /**
     * One stage of a crop, with what it inherits from the stages it names as {@code parent} filled in.
     *
     * @param growthChance the percent chance of growing on a random tick
     * @param light the light it grows in
     * @param temperature the biome temperature it grows in
     * @param bonemealChance the percent chance bone meal grows it (1.12's {@code fertilizer} of {@code minecraft:dye} 15)
     * @param canRollback whether it goes back a stage (and dies at the first) when it can't grow
     */
    public record CropStageDef(int age, StateDef state, @Nullable Range growthChance, @Nullable Range light, @Nullable Range temperature,
        @Nullable HydrationDef hydration, @Nullable Range bonemealChance, boolean canRollback) {
    }

    /**
     * What keeps a crop's soil watered: one of the blocks, with property values in their ranges, within the radius of the soil (on its
     * level).
     */
    public record HydrationDef(List<HydrationBlockDef> blocks, int radius) {
    }

    public record HydrationBlockDef(String block, Map<String, Range> properties) {
    }

    /**
     * A seed ({@code content/item/seed}): the crop it plants, on what, and its chance (percent) and amount of dropping from grass.
     */
    public record SeedDef(String id, String crop, String soil, @Nullable Range grassChance, int grassAmountMin, int grassAmountMax) {
    }

    /**
     * A range for some biomes: {@code biomes} names biome ids ({@code minecraft:plains}) or whole namespaces ({@code minecraft},
     * {@code terraincontrol}); empty for the default range.
     */
    public record BiomeRange(List<String> biomes, Range range) {

        public static boolean matches(final List<String> biomes, final String biome) {
            if (biomes.isEmpty()) {
                return true;
            }
            for (final String name : biomes) {
                if (name.contains(":") ? name.equals(biome) : biome.startsWith(name + ":")) {
                    return true;
                }
            }
            return false;
        }

        /**
         * The range for a biome, as 3.5 chose it: a range naming the biome (or its namespace), else the default, else the first.
         */
        public static @Nullable Range pick(final List<BiomeRange> ranges, final String biome) {
            BiomeRange fallback = null;
            for (final BiomeRange range : ranges) {
                if (!range.biomes().isEmpty() && matches(range.biomes(), biome)) {
                    return range.range();
                }
                if (range.biomes().isEmpty() && fallback == null) {
                    fallback = range;
                }
            }
            return fallback != null ? fallback.range() : ranges.isEmpty() ? null : ranges.getFirst().range();
        }
    }

    /**
     * Leaves ({@code content/block/leaf}): the fruit leaf they grow into the air beside them ({@code spread}) at their chance, and what
     * they drop when they decay. What they drop when broken is their state's drops.
     */
    public record LeafDef(String id, @Nullable String spreadBlock, List<BiomeRange> spreadChance, List<DecayDef> decay) {
    }

    /**
     * Drops on decay, at a chance (percent) for the biome.
     */
    public record DecayDef(List<BiomeRange> chance, List<DropDef> drops) {
    }

    /**
     * A sapling ({@code content/block/sapling}) and the tree it grows ({@code content/tree}).
     */
    public record SaplingDef(String id, String tree) {
    }

    /**
     * A tree ({@code content/tree}): its trunk height for the biome (a minimum plus a variance), its log, leaves and fruit leaves (which
     * replace leaves at their chance), and its shape: {@code normal}, {@code big} (1.12's big oak) or {@code palm}.
     */
    public record TreeDef(String id, List<TreeHeightDef> heights, String log, String leaves, @Nullable String fruit, List<BiomeRange> fruitChance,
        String shape) {
    }

    public record TreeHeightDef(List<String> biomes, int min, int varianceMin, int varianceMax) {
    }

    /**
     * A block state as the content names it: the block (26.3's id) and some of its properties.
     */
    public record StateRef(String block, Map<String, String> properties) {
    }

    /**
     * Trees in world generation ({@code content/generation/feature/tree}): the tree, its chance (percent) for the biome, once a chunk, and
     * the blocks it must stand on (empty for a sapling's soil).
     */
    public record TreeGenDef(String id, String tree, List<BiomeRange> chance, List<String> requires) {
    }

    /**
     * A grass mix ({@code content/grass}): weighted entries, each one or more block states picked from evenly.
     */
    public record GrassDef(String id, List<GrassEntryDef> entries) {
    }

    public record GrassEntryDef(int weight, List<StateRef> blocks) {
    }

    /**
     * A grass mix in world generation ({@code content/generation/feature/grass}): its chance (percent) for the biome, and the blocks it
     * must stand on.
     */
    public record GrassGenDef(String id, String grass, List<BiomeRange> chance, List<String> requires) {
    }

    /**
     * Ores in world generation ({@code content/generation/underground/ore/<dimension>.json}): veins of a size, so many a chunk, below
     * height 64.
     */
    public record OreGenDef(String dimension, List<OreDef> ores) {
    }

    public record OreDef(String block, int size, int count) {
    }

    /**
     * A container ({@code content/block/container}): its slots and how many of an item each holds.
     */
    public record ContainerDef(String id, int slots, int limit) {
    }
}
