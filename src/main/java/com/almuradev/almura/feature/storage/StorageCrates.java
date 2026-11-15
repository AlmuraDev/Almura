/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage;

import com.almuradev.almura.Almura;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Almura's storage crates (oak, spruce and birch) and the dock chest, as 3.5's {@code StorageFeature}: 54-slot blocks in the Storage tab.
 * 4.0's content loader already gives the barrels; these are the ones 3.5 made in code.
 */
public final class StorageCrates {

    private static final String TAB = Almura.ID + ":storage";
    private static final List<String> IDS = List.of("storage/dock_chest", "storage/oak_storage_crate", "storage/spruce_storage_crate",
        "storage/birch_storage_crate");
    private static final Map<Identifier, StorageCrateBlock> blocks = new LinkedHashMap<>();
    private static BlockEntityType<StorageCrateBlockEntity> blockEntityType;

    private StorageCrates() {
    }

    public static BlockEntityType<StorageCrateBlockEntity> blockEntityType() {
        return Objects.requireNonNull(blockEntityType, "The storage crates' block entity isn't registered yet");
    }

    /**
     * Registers the blocks: 3.5's hardness 6 and resistance 2000, wood.
     */
    public static void registerBlocks(final RegisterEvent.RegisterHelper<Block> helper) {
        for (final String path : IDS) {
            final Identifier id = Identifier.fromNamespaceAndPath(Almura.ID, path);
            final StorageCrateBlock block = new StorageCrateBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(6f, 2000f).mapColor(MapColor.WOOD).sound(SoundType.WOOD));
            blocks.put(id, block);
            helper.register(id, block);
        }
    }

    /**
     * Registers their items, added to the Storage tab.
     */
    public static void registerItems(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        for (final Map.Entry<Identifier, StorageCrateBlock> entry : blocks.entrySet()) {
            final Item item = new BlockItem(entry.getValue(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, entry.getKey()))
                .useBlockDescriptionPrefix());
            helper.register(entry.getKey(), item);
            addToTab.accept(TAB, item);
        }
    }

    public static void registerBlockEntityType(final RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        blockEntityType = new BlockEntityType<>(StorageCrateBlockEntity::new, new HashSet<>(blocks.values()));
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "storage_crate"), blockEntityType);
    }
}
