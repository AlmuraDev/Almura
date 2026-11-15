/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Items for the exchange on the server: one of an item kept as JSON in the database (the game's item codec, its components included,
 * replacing 3.5's item name, metadata and NBT columns), and moving items in and out of a player's main inventory (the 36 slots, as 3.5's
 * item handler did).
 */
final class ExchangeItems {

    private ExchangeItems() {
    }

    /**
     * @return one of the item, as JSON
     */
    static String encode(final HolderLookup.Provider registries, final ItemStack item) {
        final JsonElement json = ItemStack.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), item.copyWithCount(1))
            .getOrThrow();
        return json.toString();
    }

    /**
     * @return the item the JSON holds, as one of it, or empty if it can't be read (a mod removed)
     */
    static Optional<ItemStack> decode(final HolderLookup.Provider registries, final String json) {
        try {
            return ItemStack.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).result()
                .map(item -> item.copyWithCount(1));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }

    static boolean same(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    /**
     * @return how many of the item the player's main inventory holds
     */
    static int count(final ServerPlayer player, final ItemStack item) {
        final Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && same(stack, item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * @return how many of the item fit in the player's main inventory
     */
    static int space(final ServerPlayer player, final ItemStack item) {
        final Inventory inventory = player.getInventory();
        final int max = item.getMaxStackSize();
        int space = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                space += max;
            } else if (same(stack, item)) {
                space += Math.max(0, max - stack.getCount());
            }
        }
        return space;
    }

    /**
     * Takes up to {@code quantity} of the item out of the player's main inventory.
     *
     * @return how many were taken
     */
    static int extract(final ServerPlayer player, final ItemStack item, final int quantity) {
        final Inventory inventory = player.getInventory();
        int left = quantity;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && left > 0; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && same(stack, item)) {
                final int taken = Math.min(left, stack.getCount());
                stack.shrink(taken);
                if (stack.isEmpty()) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
                left -= taken;
            }
        }
        return quantity - left;
    }

    /**
     * Gives the player {@code quantity} of the item, in stacks; what doesn't fit is dropped at their feet (3.5 left that case to do).
     */
    static void give(final ServerPlayer player, final ItemStack item, final int quantity) {
        int left = quantity;
        final int max = item.getMaxStackSize();
        while (left > 0) {
            final int count = Math.min(left, max);
            final ItemStack stack = item.copyWithCount(count);
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                player.drop(stack, false, Prediction.SERVER_ONLY);
            }
            left -= count;
        }
    }

    static String name(final @Nullable ItemStack item) {
        return item == null ? "" : item.getHoverName().getString();
    }
}
