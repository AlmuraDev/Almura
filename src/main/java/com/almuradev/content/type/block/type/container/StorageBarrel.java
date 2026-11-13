/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.action.component.drop.ItemDrop;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.AbstractBlockStateDefinition;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.leaf.LeafBlockImpl;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * The storage barrel (the content's {@code container/seed_barrel}): it holds seeds, crops and tree fruit only, and its label shows what's in its first
 * slot. It takes the place of the barrels made for a single crop ({@code container/<crop>_seed_barrel}), which turn into it when opened.
 */
public final class StorageBarrel {

    public static final ResourceLocation ID = new ResourceLocation("almura", "container/seed_barrel");
    private static final String CROP_BARREL_SUFFIX = "_seed_barrel";

    /** What the label shows: the first slot's item (one of it), or empty */
    public static final IUnlistedProperty<ItemStack> LABEL = new IUnlistedProperty<ItemStack>() {
        @Override
        public String getName() {
            return "label";
        }

        @Override
        public boolean isValid(final ItemStack value) {
            return value != null;
        }

        @Override
        public Class<ItemStack> getType() {
            return ItemStack.class;
        }

        @Override
        public String valueToString(final ItemStack value) {
            return value.isEmpty() ? "empty" : value.getItem().getRegistryName() + "@" + value.getMetadata();
        }
    };

    // Whether an item (and its metadata) is a seed or crop, worked out once: hoppers ask about every stack they move
    private static final Map<String, Boolean> ACCEPTS = new ConcurrentHashMap<>();
    @Nullable private static volatile List<CropBlockImpl> crops;
    @Nullable private static volatile List<LeafBlockImpl> fruitLeaves;
    private static final String FRUIT_LEAF_SUFFIX = "_fruit_leaf";

    private StorageBarrel() {
    }

    public static boolean is(@Nullable final Block block) {
        return block != null && ID.equals(block.getRegistryName());
    }

    /**
     * @return true for the barrels made for a single crop, which have been replaced by the storage barrel
     */
    public static boolean isCropBarrel(@Nullable final Block block) {
        final ResourceLocation id = block == null ? null : block.getRegistryName();
        return id != null && "almura".equals(id.getNamespace()) && id.getPath().startsWith("container/")
                && id.getPath().endsWith(CROP_BARREL_SUFFIX) && !ID.equals(id);
    }

    /**
     * @return true when the stack is a seed, crop or tree fruit: what a crop is planted from or drops, or a fruit tree's leaves drop, Almura's or Minecraft's
     */
    public static boolean accepts(final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        return ACCEPTS.computeIfAbsent(stack.getItem().getRegistryName() + "@" + stack.getMetadata(), k -> isSeedOrCrop(stack));
    }

    private static boolean isSeedOrCrop(final ItemStack stack) {
        final Item item = stack.getItem();
        if (item == Items.WHEAT_SEEDS || item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO || item == Items.POISONOUS_POTATO
                || item == Items.BEETROOT_SEEDS || item == Items.BEETROOT || item == Items.MELON_SEEDS || item == Items.MELON
                || item == Items.PUMPKIN_SEEDS || item == Item.getItemFromBlock(Blocks.PUMPKIN) || item == Items.NETHER_WART
                || item == Items.REEDS || item == Items.APPLE || (item == Items.DYE && stack.getMetadata() == 3)) { // Cocoa beans are dye 3
            return true;
        }
        for (final CropBlockImpl crop : crops()) {
            if (crop.isCropItem(stack)) {
                return true;
            }
        }
        // Tree fruit and nuts: what a fruit tree's fruit bearing leaves drop
        for (final LeafBlockImpl leaf : fruitLeaves()) {
            if (drops(leaf.definition(leaf.getDefaultState()), stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean drops(@Nullable final AbstractBlockStateDefinition<?, ?, ?> definition, final ItemStack stack) {
        final BlockDestroyAction destroy = definition == null || definition.destroyAction == null ? null : definition.destroyAction.get();
        if (destroy == null) {
            return false;
        }
        for (final BlockDestroyAction.Entry entry : destroy.entries()) {
            for (final Drop drop : entry.drops()) {
                if (drop instanceof ItemDrop && ((ItemDrop) drop).item().test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<LeafBlockImpl> fruitLeaves() {
        List<LeafBlockImpl> list = fruitLeaves;
        if (list == null) {
            list = new ArrayList<>();
            for (final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
                if (block instanceof LeafBlockImpl && block.getRegistryName() != null && block.getRegistryName().getPath().endsWith(FRUIT_LEAF_SUFFIX)) {
                    list.add((LeafBlockImpl) block);
                }
            }
            fruitLeaves = list;
        }
        return list;
    }

    private static List<CropBlockImpl> crops() {
        List<CropBlockImpl> list = crops;
        if (list == null) {
            list = new ArrayList<>();
            for (final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
                if (block instanceof CropBlockImpl) {
                    list.add((CropBlockImpl) block);
                }
            }
            crops = list;
        }
        return list;
    }
}
