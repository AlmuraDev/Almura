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
}
