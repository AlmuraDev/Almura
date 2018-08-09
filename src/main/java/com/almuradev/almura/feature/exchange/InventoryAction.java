/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.shared.item.VanillaStack;
import com.google.common.base.MoreObjects;

public final class InventoryAction {

    private final Direction direction;
    private final VanillaStack stack;

    public InventoryAction(final Direction direction, final VanillaStack stack) {
        this.direction = direction;
        this.stack = stack;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public VanillaStack getStack() {
        return this.stack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("stack", this.stack)
            .add("direction", this.direction)
            .toString();
    }

    public enum Direction {
        TO_LISTING,
        TO_INVENTORY
    }
}
