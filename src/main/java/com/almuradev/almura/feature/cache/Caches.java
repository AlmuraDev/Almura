/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import com.almuradev.almura.Almura;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Almura's caches ({@code almura:cache/wood} to {@code almura:cache/ender}), as 3.5's {@code CacheFeature}: their blocks, items (in the
 * Storage tab), block entity, upgrade recipe and item capability.
 */
public final class Caches {

    private static final String TAB = Almura.ID + ":storage";
    private static final Map<CacheBlock.Tier, CacheBlock> blocks = new EnumMap<>(CacheBlock.Tier.class);
    private static BlockEntityType<CacheBlockEntity> blockEntityType;

    private Caches() {
    }

    public static Identifier id(final CacheBlock.Tier tier) {
        return Identifier.fromNamespaceAndPath(Almura.ID, "cache/" + tier.id());
    }

    public static BlockEntityType<CacheBlockEntity> blockEntityType() {
        return Objects.requireNonNull(blockEntityType, "The caches' block entity isn't registered yet");
    }

    /**
     * Registers the caches' blocks: 3.5's hardness 6 and resistance 2000.
     */
    public static void registerBlocks(final RegisterEvent.RegisterHelper<Block> helper) {
        for (final CacheBlock.Tier tier : CacheBlock.Tier.values()) {
            final CacheBlock block = new CacheBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id(tier)))
                .strength(6f, 2000f).mapColor(MapColor.WOOD).sound(tier == CacheBlock.Tier.WOOD ? SoundType.WOOD : SoundType.METAL), tier);
            blocks.put(tier, block);
            helper.register(id(tier), block);
        }
    }

    /**
     * Registers the caches' items, added to the Storage tab.
     */
    public static void registerItems(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        for (final Map.Entry<CacheBlock.Tier, CacheBlock> entry : blocks.entrySet()) {
            final Item item = new CacheItem(entry.getValue(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(entry.getKey())))
                .useBlockDescriptionPrefix().stacksTo(1));
            helper.register(id(entry.getKey()), item);
            addToTab.accept(TAB, item);
        }
    }

    public static void registerBlockEntityType(final RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        blockEntityType = new BlockEntityType<>(CacheBlockEntity::new, new HashSet<>(blocks.values()));
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "cache"), blockEntityType);
    }

    public static void registerRecipeSerializer(final RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "cache_upgrade"), CacheUpgradeRecipe.SERIALIZER);
    }

    /**
     * Hoppers, pipes and the like reach a cache's one slot through NeoForge's item capability, minding its limit.
     */
    public static void onRegisterCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, blockEntityType(), (cache, side) -> cache.handler());
    }
}
