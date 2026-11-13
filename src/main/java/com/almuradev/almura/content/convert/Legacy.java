/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.convert;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * 1.12 names that 1.13 renamed, for the ones the content uses: vanilla textures (and the {@code blocks/}, {@code items/} folders), and items
 * that had metadata.
 */
public final class Legacy {

    // 1.12 texture name (without the folder) to 1.13's
    private static final Map<String, String> TEXTURES = Map.ofEntries(
        Map.entry("stonebrick", "stone_bricks"),
        Map.entry("stonebrick_mossy", "mossy_stone_bricks"),
        Map.entry("stonebrick_cracked", "cracked_stone_bricks"),
        Map.entry("stonebrick_carved", "chiseled_stone_bricks"),
        Map.entry("ice_packed", "packed_ice"),
        Map.entry("planks_oak", "oak_planks"),
        Map.entry("planks_spruce", "spruce_planks"),
        Map.entry("planks_birch", "birch_planks"),
        Map.entry("planks_jungle", "jungle_planks"),
        Map.entry("planks_acacia", "acacia_planks"),
        Map.entry("planks_big_oak", "dark_oak_planks"),
        Map.entry("cobblestone_mossy", "mossy_cobblestone"),
        Map.entry("water_still", "water_still"),
        Map.entry("lava_still", "lava_still"),
        Map.entry("brick", "bricks"),
        Map.entry("wool_colored_white", "white_wool"),
        Map.entry("hardened_clay", "terracotta"));

    // 1.12 item id with metadata to 1.13's id
    private static final Map<String, String> ITEMS = Map.ofEntries(
        Map.entry("minecraft:dye:0", "minecraft:ink_sac"),
        Map.entry("minecraft:dye:1", "minecraft:red_dye"),
        Map.entry("minecraft:dye:2", "minecraft:green_dye"),
        Map.entry("minecraft:dye:3", "minecraft:cocoa_beans"),
        Map.entry("minecraft:dye:4", "minecraft:lapis_lazuli"),
        Map.entry("minecraft:dye:5", "minecraft:purple_dye"),
        Map.entry("minecraft:dye:6", "minecraft:cyan_dye"),
        Map.entry("minecraft:dye:7", "minecraft:light_gray_dye"),
        Map.entry("minecraft:dye:8", "minecraft:gray_dye"),
        Map.entry("minecraft:dye:9", "minecraft:pink_dye"),
        Map.entry("minecraft:dye:10", "minecraft:lime_dye"),
        Map.entry("minecraft:dye:11", "minecraft:yellow_dye"),
        Map.entry("minecraft:dye:12", "minecraft:light_blue_dye"),
        Map.entry("minecraft:dye:13", "minecraft:magenta_dye"),
        Map.entry("minecraft:dye:14", "minecraft:orange_dye"),
        Map.entry("minecraft:dye:15", "minecraft:bone_meal"),
        Map.entry("minecraft:coal:1", "minecraft:charcoal"),
        Map.entry("minecraft:fish:0", "minecraft:cod"),
        Map.entry("minecraft:fish:1", "minecraft:salmon"),
        Map.entry("minecraft:fish:2", "minecraft:tropical_fish"),
        Map.entry("minecraft:fish:3", "minecraft:pufferfish"),
        Map.entry("minecraft:cooked_fish:0", "minecraft:cooked_cod"),
        Map.entry("minecraft:cooked_fish:1", "minecraft:cooked_salmon"),
        Map.entry("minecraft:planks:0", "minecraft:oak_planks"),
        Map.entry("minecraft:planks:1", "minecraft:spruce_planks"),
        Map.entry("minecraft:planks:2", "minecraft:birch_planks"),
        Map.entry("minecraft:planks:3", "minecraft:jungle_planks"),
        Map.entry("minecraft:planks:4", "minecraft:acacia_planks"),
        Map.entry("minecraft:planks:5", "minecraft:dark_oak_planks"),
        Map.entry("minecraft:log:0", "minecraft:oak_log"),
        Map.entry("minecraft:log:1", "minecraft:spruce_log"),
        Map.entry("minecraft:log:2", "minecraft:birch_log"),
        Map.entry("minecraft:log:3", "minecraft:jungle_log"),
        Map.entry("minecraft:log2:0", "minecraft:acacia_log"),
        Map.entry("minecraft:log2:1", "minecraft:dark_oak_log"),
        Map.entry("minecraft:wool:0", "minecraft:white_wool"),
        Map.entry("minecraft:stone:0", "minecraft:stone"),
        Map.entry("minecraft:stone:1", "minecraft:granite"),
        Map.entry("minecraft:stone:3", "minecraft:diorite"),
        Map.entry("minecraft:stone:5", "minecraft:andesite"),
        Map.entry("minecraft:sand:0", "minecraft:sand"),
        Map.entry("minecraft:sand:1", "minecraft:red_sand"),
        Map.entry("minecraft:sapling:0", "minecraft:oak_sapling"),
        Map.entry("minecraft:leaves:0", "minecraft:oak_leaves"),
        Map.entry("minecraft:stonebrick:0", "minecraft:stone_bricks"),
        Map.entry("minecraft:stonebrick:1", "minecraft:mossy_stone_bricks"),
        Map.entry("minecraft:stonebrick:2", "minecraft:cracked_stone_bricks"),
        Map.entry("minecraft:stonebrick:3", "minecraft:chiseled_stone_bricks"),
        Map.entry("minecraft:red_flower:0", "minecraft:poppy"),
        Map.entry("minecraft:golden_apple:0", "minecraft:golden_apple"),
        Map.entry("minecraft:bed:0", "minecraft:white_bed"),
        Map.entry("minecraft:bed:14", "minecraft:red_bed"),
        Map.entry("minecraft:potion:0", "minecraft:potion"));

    // 1.12 item ids renamed in 1.13 (whatever their metadata)
    private static final Map<String, String> RENAMED = Map.ofEntries(
        Map.entry("minecraft:reeds", "minecraft:sugar_cane"),
        Map.entry("minecraft:waterlily", "minecraft:lily_pad"),
        Map.entry("minecraft:speckled_melon", "minecraft:glistering_melon_slice"),
        Map.entry("minecraft:melon", "minecraft:melon_slice"),
        Map.entry("minecraft:netherbrick", "minecraft:nether_brick"),
        Map.entry("minecraft:fireworks", "minecraft:firework_rocket"),
        Map.entry("minecraft:boat", "minecraft:oak_boat"),
        Map.entry("minecraft:wooden_door", "minecraft:oak_door"),
        Map.entry("minecraft:sign", "minecraft:oak_sign"),
        Map.entry("minecraft:snowball", "minecraft:snowball"),
        Map.entry("minecraft:carrots", "minecraft:carrot"),
        Map.entry("minecraft:potatoes", "minecraft:potato"),
        Map.entry("minecraft:pumpkin", "minecraft:carved_pumpkin"),
        Map.entry("minecraft:web", "minecraft:cobweb"),
        Map.entry("minecraft:tallgrass", "minecraft:short_grass"),
        Map.entry("minecraft:yellow_flower", "minecraft:dandelion"),
        Map.entry("minecraft:hardened_clay", "minecraft:terracotta"),
        Map.entry("minecraft:stone_slab", "minecraft:smooth_stone_slab"));

    // 1.12's "any metadata" (32767) for items that came apart in 1.13: the tag, or the item, that holds them all now
    private static final Map<String, String> ANY = Map.ofEntries(
        Map.entry("minecraft:dye", "#c:dyes"),
        Map.entry("minecraft:bed", "#minecraft:beds"),
        Map.entry("minecraft:wool", "#minecraft:wool"),
        Map.entry("minecraft:planks", "#minecraft:planks"),
        Map.entry("minecraft:log", "#minecraft:logs"),
        Map.entry("minecraft:log2", "#minecraft:logs"),
        Map.entry("minecraft:sapling", "#minecraft:saplings"),
        Map.entry("minecraft:leaves", "#minecraft:leaves"),
        Map.entry("minecraft:sand", "#minecraft:sand"),
        Map.entry("minecraft:potion", "minecraft:potion"),
        Map.entry("minecraft:coal", "#minecraft:coals"),
        Map.entry("minecraft:fish", "#minecraft:fishes"));

    private static final int ANY_DATA = 32767;

    private Legacy() {
    }

    /**
     * A texture reference as 26.3 names it: {@code minecraft:blocks/stonebrick_mossy} as {@code minecraft:block/mossy_stone_bricks}. Almura's
     * own and other references are left as they are.
     */
    public static String texture(final String reference) {
        if (reference.startsWith("#")) {
            return reference;
        }
        final boolean vanilla = !reference.contains(":") || reference.startsWith("minecraft:");
        String path = reference.contains(":") ? reference.substring(reference.indexOf(':') + 1) : reference;
        // 1.12 read "almura:/block/x" as "almura:block/x"; 26.3 doesn't
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!vanilla) {
            return reference.substring(0, reference.indexOf(':') + 1) + path;
        }
        String folder = null, name = path;
        if (path.startsWith("blocks/")) {
            folder = "block/";
            name = path.substring("blocks/".length());
        } else if (path.startsWith("items/")) {
            folder = "item/";
            name = path.substring("items/".length());
        }
        if (folder == null) {
            return reference;
        }
        return "minecraft:" + folder + TEXTURES.getOrDefault(name, name);
    }

    /**
     * An item reference as 26.3 names it.
     *
     * @param data the 1.12 metadata (0 when none)
     * @return The item's id (or a tag, {@code #c:dyes}, for 1.12's "any metadata"), or null when 1.12's had metadata this doesn't know
     */
    public static @Nullable String item(final String id, final int data) {
        final String full = id.contains(":") ? id : "minecraft:" + id;
        if (!full.startsWith("minecraft:")) {
            return full;
        }
        if (data == ANY_DATA) {
            return ANY.getOrDefault(full, RENAMED.getOrDefault(full, full));
        }
        final String mapped = ITEMS.get(full + ":" + data);
        if (mapped != null) {
            return mapped;
        }
        if (data != 0 && ITEMS.containsKey(full + ":0")) {
            return null;
        }
        return RENAMED.getOrDefault(full, full);
    }
}
