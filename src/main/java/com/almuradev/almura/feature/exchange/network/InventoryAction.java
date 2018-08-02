/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import net.minecraft.item.ItemStack;

public final class InventoryAction {

    private final Direction direction;
    private final ItemStack stack;

    public InventoryAction(final Direction direction, final ItemStack stack) {
        this.direction = direction;
        this.stack = stack;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public enum Direction {
        TO_LISTING,
        TO_INVENTORY
    }
}
