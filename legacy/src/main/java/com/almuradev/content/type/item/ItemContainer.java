/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * What an item leaves behind when a recipe uses it (Forge's container item): another item (a bottle, bowl, cruet, ...), or the item
 * itself with some damage (knives, graters, ...), which is used up once worn out.
 *
 * <p>Configured with the {@code container} key: {@code "container": "minecraft:glass_bottle"} or {@code "container": { "damage": 1 }}.</p>
 */
public final class ItemContainer {

    @Nullable private final ResourceLocation itemId;
    private final int damage;
    @Nullable private Item item;

    private ItemContainer(@Nullable final ResourceLocation itemId, final int damage) {
        this.itemId = itemId;
        this.damage = damage;
    }

    public static ItemContainer of(final ResourceLocation itemId) {
        return new ItemContainer(itemId, 0);
    }

    public static ItemContainer damaged(final int damage) {
        return new ItemContainer(null, damage);
    }

    /**
     * @param used The stack the recipe used
     * @return What is left behind, empty for nothing
     */
    public ItemStack getContainerFor(final ItemStack used) {
        if (this.itemId != null) {
            if (this.item == null) {
                this.item = ForgeRegistries.ITEMS.getValue(this.itemId);
            }
            return this.item == null ? ItemStack.EMPTY : new ItemStack(this.item);
        }

        final ItemStack copy = used.copy();
        copy.setCount(1);
        if (!copy.isItemStackDamageable() || this.damage <= 0) {
            return copy;
        }

        final int next = copy.getItemDamage() + this.damage;
        if (next >= copy.getMaxDamage()) {
            return ItemStack.EMPTY; // Worn out
        }
        copy.setItemDamage(next);
        return copy;
    }
}
