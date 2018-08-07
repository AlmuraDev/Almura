/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.shared.item.VirtualStack;

public final class InventoryAction {

    private final Direction direction;
    private final VirtualStack stack;

    public InventoryAction(final Direction direction, final VirtualStack stack) {
        this.direction = direction;
        this.stack = stack;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public VirtualStack getStack() {
        return this.stack;
    }

    public enum Direction {
        TO_LISTING,
        TO_INVENTORY
    }
}
