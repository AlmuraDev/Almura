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
        final long blocks = 3 + 764 + 22 + 99 + 128 + 110 + 42 + 25 + 69;
        final long crops = 85;
        assertEquals(blocks + crops, count("almura", BuiltInRegistries.BLOCK.keySet()), "blocks");
        // Normal, food, hoe, sickle and seed items, and an item for each block but the crops (their item is their seed)
        // and Almura's three cruets, harvesting sack, Farmer's Almanac and Skills Handbook
        assertEquals(186 + 413 + 5 + 5 + 85 + blocks + 3 + 3, count("almura", BuiltInRegistries.ITEM.keySet()), "items");
        final Item cruet = BuiltInRegistries.ITEM.getValue(com.almuradev.almura.feature.cruet.Cruets.EMPTY);
        for (final Identifier filled : new Identifier[] {com.almuradev.almura.feature.cruet.Cruets.WATER, com.almuradev.almura.feature.cruet.Cruets.MILK}) {
            final var remainder = BuiltInRegistries.ITEM.getValue(filled).getCraftingRemainder();
            assertTrue(remainder != null && remainder.item().value() == cruet, filled + " hands the glass cruet back");
        }

        final Block barley = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/barley"));
        assertTrue(barley instanceof com.almuradev.almura.content.registry.ContentCrop crop && crop.getMaxAge() == 3, "barley grows in 4 stages");
        final Item barleySeed = BuiltInRegistries.ITEM.getValue(Identifier.parse("almura:seed/barley_seed"));
        assertTrue(barleySeed instanceof BlockItem seedItem && seedItem.getBlock() == barley, "barley seed plants barley");
        assertEquals("item.almura.seed.barley_seed", barleySeed.getDescriptionId());
        assertTrue(barley.defaultBlockState().isRandomlyTicking(), "crops grow");
        // The harvesting sack picks the content's sack_harvest crops and holds what they drop (components aren't bound here, so its stack
        // size of 1 can't be read)
        final Item sack = BuiltInRegistries.ITEM.getValue(com.almuradev.almura.feature.harvestsack.HarvestingSack.ID);
        assertTrue(sack instanceof com.almuradev.almura.feature.harvestsack.HarvestingSack.SackItem, "the harvesting sack is there");
        assertTrue(BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof com.almuradev.almura.content.registry.ContentCrop crop
            && crop.def().sackResetAge() >= 0).count() == 11, "11 crops are picked with the sack");
        // Aquatic crops: seaweed is always water, rice is waterlogged in water and dry in the wild, barley is a land crop
        final net.minecraft.world.level.block.state.properties.BooleanProperty waterlogged =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
        final Block seaweed = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/seaweed"));
        assertTrue(seaweed.defaultBlockState().getFluidState().isSource(), "seaweed holds water");
        final Block rice = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/rice"));
        assertTrue(rice.defaultBlockState().getFluidState().isEmpty(), "wild rice is dry");
        assertTrue(rice.defaultBlockState().setValue(waterlogged, true).getFluidState().isSource(), "paddy rice holds water");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/wasabi")).defaultBlockState().hasProperty(waterlogged),
            "wasabi grows out of water");
        assertTrue(barley.defaultBlockState().getFluidState().isEmpty() && !barley.defaultBlockState().hasProperty(waterlogged),
            "barley is a land crop");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:leaf/cherry_tree_leaf"))
            instanceof com.almuradev.almura.content.registry.ContentLeaves, "leaves are leaves");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:sapling/cherry_tree_sapling"))
            instanceof com.almuradev.almura.content.registry.ContentSapling, "saplings are saplings");
        assertTrue(com.almuradev.almura.content.registry.ContentTrees.tree("almura:cherry_tree") != null, "saplings know their trees");
        final Block storageBarrel = BuiltInRegistries.BLOCK.getValue(com.almuradev.almura.content.registry.ContentContainers.STORAGE_BARREL);
        assertTrue(storageBarrel instanceof com.almuradev.almura.content.registry.ContentContainers.ContainerBlock, "the storage barrel is a container");
        assertTrue(com.almuradev.almura.content.registry.ContentContainers.storageBarrelAccepts("almura:seed/barley_seed"), "the storage barrel takes seeds");
        assertTrue(com.almuradev.almura.content.registry.ContentContainers.storageBarrelAccepts("almura:normal/ingredient/barley"),
            "the storage barrel takes crops");
        assertTrue(!com.almuradev.almura.content.registry.ContentContainers.storageBarrelAccepts("minecraft:dirt"), "the storage barrel takes no dirt");
        assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "container")), "container block entity");
        assertTrue(BuiltInRegistries.MENU.containsKey(Identifier.fromNamespaceAndPath("almura", "storage_barrel")), "storage barrel menu");
        // Every crop's hydration names blocks and properties that are there
        final var parser = new com.almuradev.almura.content.def.ContentParser(
            com.almuradev.almura.content.source.ContentSource.at(Path.of(root)));
        final java.util.List<String> wrongHydration = new java.util.ArrayList<>();
        parser.crops(parser.seeds()).forEach(crop -> crop.stages().forEach(stage -> {
            if (stage.hydration() != null) {
                stage.hydration().blocks().forEach(block -> {
                    final Block hydrating = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(block.block())).orElse(null);
                    if (hydrating == null) {
                        wrongHydration.add(crop.id() + ": " + block.block());
                    } else {
                        block.properties().keySet().stream().filter(name -> hydrating.getStateDefinition().getProperty(name) == null)
                            .forEach(name -> wrongHydration.add(crop.id() + ": " + block.block() + " has no " + name));
                    }
                });
            }
        }));
        assertEquals(java.util.List.of(), wrongHydration, "crop hydration");
        // The farmland rules and the biome temperature crops read are mixed in
        assertTrue(com.almuradev.almura.mixin.BiomeAccessor.class.isAssignableFrom(net.minecraft.world.level.biome.Biome.class), "biome accessor");
        assertTrue(java.util.Arrays.stream(net.minecraft.world.level.block.FarmlandBlock.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("irrigationNear")), "farmland mixin");
        for (final Class<?> held : new Class<?>[] {net.minecraft.world.level.levelgen.feature.TreeFeature.class,
            net.minecraft.world.level.levelgen.feature.SimpleBlockFeature.class}) {
            assertTrue(java.util.Arrays.stream(held.getDeclaredMethods()).anyMatch(method -> method.getName().contains("heldBack")),
                held.getSimpleName() + " held back where Almura generated");
        }
        assertTrue(BuiltInRegistries.FEATURE_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "content_trees")), "tree generation");
        assertTrue(BuiltInRegistries.FEATURE_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "content_grass")), "grass generation");
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
