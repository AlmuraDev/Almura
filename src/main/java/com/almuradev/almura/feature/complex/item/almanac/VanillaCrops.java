/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Minecraft's own crops, as the Farmer's Almanac lists them next to Almura's. Used by the server (for their Skills levels) and the client
 * (for the list), so it only names blocks and items by id.
 */
public final class VanillaCrops {

    public static final List<VanillaCrop> ALL = ImmutableList.of(
            new VanillaCrop("minecraft:wheat", "Wheat Crop", "minecraft:wheat_seeds", 0, "minecraft:wheat", "minecraft:wheat", true, null),
            new VanillaCrop("minecraft:carrots", "Carrot Crop", "minecraft:carrot", 0, "minecraft:carrots", "minecraft:carrots", true, null),
            new VanillaCrop("minecraft:potatoes", "Potato Crop", "minecraft:potato", 0, "minecraft:potatoes", "minecraft:potatoes", true, null),
            new VanillaCrop("minecraft:beetroots", "Beetroot Crop", "minecraft:beetroot_seeds", 0, "minecraft:beetroots", "minecraft:beetroots", true,
                    null),
            new VanillaCrop("minecraft:melon", "Melon", "minecraft:melon_seeds", 0, "minecraft:melon_stem", "minecraft:melon_block", true,
                    "Fruit grows on a free block next to the stem"),
            new VanillaCrop("minecraft:pumpkin", "Pumpkin", "minecraft:pumpkin_seeds", 0, "minecraft:pumpkin_stem", "minecraft:pumpkin", true,
                    "Fruit grows on a free block next to the stem"),
            new VanillaCrop("minecraft:nether_wart", "Nether Wart", "minecraft:nether_wart", 0, "minecraft:nether_wart", "minecraft:nether_wart",
                    false, "Planted on soul sand"),
            // Cocoa beans are dye 3
            new VanillaCrop("minecraft:cocoa", "Cocoa", "minecraft:dye", 3, "minecraft:cocoa", "minecraft:cocoa", false, "Planted on jungle logs"),
            new VanillaCrop("minecraft:reeds", "Sugar Cane", "minecraft:reeds", 0, "minecraft:reeds", "minecraft:reeds", false,
                    "Needs water next to the block it's planted on")
    );

    private VanillaCrops() {
    }

    public static final class VanillaCrop {

        /** The id the Almanac knows the crop by */
        public final String id;
        public final String name;
        /** What it's planted from */
        public final String seedItem;
        public final int seedMeta;
        /** The block that's planted, and whose stages are shown */
        public final String plantBlock;
        /** The block that's harvested (a melon or pumpkin grows next to its stem) */
        public final String harvestBlock;
        /** True when it needs a light level of 9 or more to grow, as Minecraft's field crops and stems do */
        public final boolean needsLight;
        @Nullable public final String note;

        VanillaCrop(final String id, final String name, final String seedItem, final int seedMeta, final String plantBlock, final String harvestBlock,
                final boolean needsLight, @Nullable final String note) {
            this.id = id;
            this.name = name;
            this.seedItem = seedItem;
            this.seedMeta = seedMeta;
            this.plantBlock = plantBlock;
            this.harvestBlock = harvestBlock;
            this.needsLight = needsLight;
            this.note = note;
        }
    }
}
