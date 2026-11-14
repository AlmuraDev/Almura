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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * With the game's mod loading started (FML's JUnit extension, set up by the build), the content's blocks, items and creative tabs are in
 * the registries.
 */
final class ContentRegistrationTest {

    private static long count(final String namespace, final Iterable<Identifier> ids) {
        long count = 0;
        for (final Identifier id : ids) {
            if (id.getNamespace().equals(namespace)) {
                count++;
            }
        }
        return count;
    }

    @Test
    void registersTheContent() {
        final String root = System.getProperty("almura.content");
        assumeTrue(root != null && Files.isDirectory(Path.of(root)), "The content isn't in the dev run");
        // Normal, horizontal, log, flower, stair and single slab blocks (a double slab is its single's double state), and crops
        final long blocks = 3 + 764 + 22 + 99 + 128 + 110;
        final long crops = 85;
        assertEquals(blocks + crops, count("almura", BuiltInRegistries.BLOCK.keySet()), "blocks");
        // Normal, food, hoe, sickle and seed items, and an item for each block but the crops (their item is their seed)
        assertEquals(186 + 413 + 5 + 5 + 85 + blocks, count("almura", BuiltInRegistries.ITEM.keySet()), "items");

        final Block barley = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/barley"));
        assertTrue(barley instanceof com.almuradev.almura.content.registry.ContentCrop crop && crop.getMaxAge() == 3, "barley grows in 4 stages");
        final Item barleySeed = BuiltInRegistries.ITEM.getValue(Identifier.parse("almura:seed/barley_seed"));
        assertTrue(barleySeed instanceof BlockItem seedItem && seedItem.getBlock() == barley, "barley seed plants barley");
        assertEquals("item.almura.seed.barley_seed", barleySeed.getDescriptionId());
        assertTrue(barley.defaultBlockState().isRandomlyTicking(), "crops grow");
        // The farmland rules and the biome temperature crops read are mixed in
        assertTrue(com.almuradev.almura.mixin.BiomeAccessor.class.isAssignableFrom(net.minecraft.world.level.biome.Biome.class), "biome accessor");
        assertTrue(java.util.Arrays.stream(net.minecraft.world.level.block.FarmlandBlock.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("irrigationNear")), "farmland mixin");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:slab/afledos_slab")) instanceof net.minecraft.world.level.block.SlabBlock,
            "slabs are slabs");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:stair/afledos_stair")) instanceof net.minecraft.world.level.block.StairBlock,
            "stairs are stairs");
        assertEquals(23, count("almura", BuiltInRegistries.CREATIVE_MODE_TAB.keySet()), "creative tabs");
        assertTrue(BuiltInRegistries.CONSUME_EFFECT_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "heal")), "heal effect");

        final Block plank = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:horizontal/building/afledos_plank"));
        assertTrue(plank instanceof HorizontalDirectionalBlock, "a horizontal block faces");
        final Item plankItem = BuiltInRegistries.ITEM.getValue(Identifier.parse("almura:horizontal/building/afledos_plank"));
        assertTrue(plankItem instanceof BlockItem blockItem && blockItem.getBlock() == plank, "its item places it");
        assertEquals("block.almura.horizontal.building.afledos_plank", plank.getDescriptionId());

        final Item pizza = BuiltInRegistries.ITEM.getValue(Identifier.parse("almura:food/food/pizza_deluxe"));
        assertEquals("item.almura.food.food.pizza_deluxe", pizza.getDescriptionId());
    }
}
