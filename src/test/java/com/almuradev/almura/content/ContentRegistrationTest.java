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
        assertEquals(3 + 764, count("almura", BuiltInRegistries.BLOCK.keySet()), "blocks");
        // Normal and food items, and an item for each block
        assertEquals(186 + 413 + 3 + 764, count("almura", BuiltInRegistries.ITEM.keySet()), "items");
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
