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

import com.almuradev.almura.content.def.ContentDefs;
import com.almuradev.almura.content.def.ContentParser;
import com.almuradev.almura.content.registry.ContentBlocks;
import com.almuradev.almura.content.registry.ContentContainers;
import com.almuradev.almura.content.registry.ContentCrop;
import com.almuradev.almura.content.registry.ContentLeaves;
import com.almuradev.almura.content.registry.ContentSapling;
import com.almuradev.almura.content.registry.ContentTrees;
import com.almuradev.almura.content.source.ContentSource;
import com.almuradev.almura.feature.cruet.Cruets;
import com.almuradev.almura.feature.harvestsack.HarvestingSack;
import com.almuradev.almura.mixin.BiomeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.material.PushReaction;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // And Almura's Coin Exchange
        final long coinExchange = 1;
        // And its six caches, three storage crates and dock chest
        final long cachesAndCrates = 10;
        assertEquals(blocks + crops + coinExchange + cachesAndCrates, count("almura", BuiltInRegistries.BLOCK.keySet()), "blocks");
        // Normal, food, hoe, sickle and seed items, and an item for each block but the crops (their item is their seed)
        // and Almura's three cruets, harvesting sack, Farmer's Almanac, Skills Handbook, Coin Exchange, caches and crates
        assertEquals(186 + 413 + 5 + 5 + 85 + blocks + 3 + 3 + coinExchange + cachesAndCrates, count("almura", BuiltInRegistries.ITEM.keySet()), "items");
        final Item cruet = BuiltInRegistries.ITEM.getValue(Cruets.EMPTY);
        for (final Identifier filled : new Identifier[] {Cruets.WATER, Cruets.MILK}) {
            final var remainder = BuiltInRegistries.ITEM.getValue(filled).getCraftingRemainder();
            assertTrue(remainder != null && remainder.item().value() == cruet, filled + " hands the glass cruet back");
        }

        final Block barley = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:crop/barley"));
        assertTrue(barley instanceof ContentCrop crop && crop.getMaxAge() == 3, "barley grows in 4 stages");
        final Item barleySeed = BuiltInRegistries.ITEM.getValue(Identifier.parse("almura:seed/barley_seed"));
        assertTrue(barleySeed instanceof BlockItem seedItem && seedItem.getBlock() == barley, "barley seed plants barley");
        assertEquals("item.almura.seed.barley_seed", barleySeed.getDescriptionId());
        assertTrue(barley.defaultBlockState().isRandomlyTicking(), "crops grow");
        // The harvesting sack picks the content's sack_harvest crops and holds what they drop (components aren't bound here, so its stack
        // size of 1 can't be read)
        final Item sack = BuiltInRegistries.ITEM.getValue(HarvestingSack.ID);
        assertTrue(sack instanceof HarvestingSack.SackItem, "the harvesting sack is there");
        assertTrue(BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof ContentCrop crop
            && crop.def().sackResetAge() >= 0).count() == 11, "11 crops are picked with the sack");
        // Aquatic crops: seaweed is always water, rice is waterlogged in water and dry in the wild, barley is a land crop
        final BooleanProperty waterlogged =
            BlockStateProperties.WATERLOGGED;
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
            instanceof ContentLeaves, "leaves are leaves");
        // The 13 flickering lights mark themselves with a block entity, so the client can make them flicker
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:horizontal/lighting/hanging_lamp_flicker"))
            instanceof EntityBlock, "a flickering light has a block entity");
        assertTrue(!(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:horizontal/agriculture/pipe"))
            instanceof EntityBlock), "a pipe doesn't");
        assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "flicker_light")), "flicker light marker");
        // The content's own sound group (barrel: gravel break, wool place, at 0.1) and material (ag: not solid, broken when pushed)
        // (no block names the barrel group as its sound yet; it is read for those that will)
        final SoundType barrelSound = ContentBlocks.soundGroup("almura:barrel");
        assertTrue(barrelSound != null, "the barrel sound group is read");
        assertEquals(0.1f, barrelSound.getVolume(), 0.0001f, "barrel sound volume");
        assertEquals(SoundEvents.GRAVEL_BREAK, barrelSound.getBreakSound(), "barrel break sound");
        assertEquals(SoundEvents.WOOL_PLACE, barrelSound.getPlaceSound(), "barrel place sound (1.12's block.cloth.place)");
        final Block pipe = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:horizontal/agriculture/pipe"));
        assertEquals(PushReaction.POPPED, pipe.defaultBlockState().getPistonPushReaction(), "pipes pop when pushed");
        assertTrue(!pipe.defaultBlockState().isSolid(), "pipes aren't solid");
        // Leaves burn as the content says (flammability 30, fire_spread_speed 60), as 3.5 gave them to Forge
        final Block almond = BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:leaf/almond_tree_leaf"));
        assertEquals(30, almond.getFlammability(almond.defaultBlockState(), EmptyBlockGetter.INSTANCE,
            BlockPos.ZERO, Direction.UP), "leaf flammability");
        assertEquals(60, almond.getFireSpreadSpeed(almond.defaultBlockState(), EmptyBlockGetter.INSTANCE,
            BlockPos.ZERO, Direction.UP), "leaf fire spread speed");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:sapling/cherry_tree_sapling"))
            instanceof ContentSapling, "saplings are saplings");
        assertTrue(ContentTrees.tree("almura:cherry_tree") != null, "saplings know their trees");
        final Block storageBarrel = BuiltInRegistries.BLOCK.getValue(ContentContainers.STORAGE_BARREL);
        assertTrue(storageBarrel instanceof ContentContainers.ContainerBlock, "the storage barrel is a container");
        assertTrue(ContentContainers.storageBarrelAccepts("almura:seed/barley_seed"), "the storage barrel takes seeds");
        assertTrue(ContentContainers.storageBarrelAccepts("almura:normal/ingredient/barley"),
            "the storage barrel takes crops");
        assertTrue(!ContentContainers.storageBarrelAccepts("minecraft:dirt"), "the storage barrel takes no dirt");
        assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "container")), "container block entity");
        assertTrue(BuiltInRegistries.MENU.containsKey(Identifier.fromNamespaceAndPath("almura", "storage_barrel")), "storage barrel menu");
        // Every crop's hydration names blocks and properties that are there
        final var parser = new ContentParser(
            ContentSource.at(Path.of(root)));
        // The pipes turn to connect when placed against a block's side, and the ceiling ones drip, as on 3.5
        final Map<String, ContentDefs.StateDef> pipes = new HashMap<>();
        parser.blocks(AlmuraContent.BLOCK_KINDS).stream()
            .filter(block -> block.id().startsWith("almura:horizontal/agriculture/"))
            .forEach(block -> pipes.put(block.id().substring("almura:horizontal/agriculture/".length()), block.state()));
        assertEquals("across", pipes.get("pipe").rotatePlacement(), "a straight pipe continues out from the block");
        assertEquals("toward", pipes.get("t_pipe").rotatePlacement(), "a T pipe points its branch into the block");
        assertEquals("corner", pipes.get("corner_pipe").rotatePlacement(), "a corner puts an arm into the block");
        assertEquals(null, pipes.get("four_way_pipe").rotatePlacement(), "a four way pipe faces the player");
        assertTrue(pipes.get("ceiling_pipe").drip() && !pipes.get("pipe").drip(), "ceiling pipes drip, floor ones don't");
        final List<String> wrongHydration = new ArrayList<>();
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
        assertEquals(List.of(), wrongHydration, "crop hydration");
        // The farmland rules and the biome temperature crops read are mixed in
        assertTrue(BiomeAccessor.class.isAssignableFrom(Biome.class), "biome accessor");
        assertTrue(Arrays.stream(FarmlandBlock.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("irrigationNear")), "farmland mixin");
        for (final Class<?> held : new Class<?>[] {TreeFeature.class, SimpleBlockFeature.class}) {
            assertTrue(Arrays.stream(held.getDeclaredMethods()).anyMatch(method -> method.getName().contains("heldBack")),
                held.getSimpleName() + " held back where Almura generated");
        }
        assertTrue(BuiltInRegistries.FEATURE_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "content_trees")), "tree generation");
        assertTrue(BuiltInRegistries.FEATURE_TYPE.containsKey(Identifier.fromNamespaceAndPath("almura", "content_grass")), "grass generation");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:slab/afledos_slab")) instanceof SlabBlock,
            "slabs are slabs");
        assertTrue(BuiltInRegistries.BLOCK.getValue(Identifier.parse("almura:stair/afledos_stair")) instanceof StairBlock,
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
