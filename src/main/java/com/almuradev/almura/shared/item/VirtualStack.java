/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * An immutable-enough representation of an ItemStack-like concept that can be implemented by a variety of objects.
 */
public interface VirtualStack {

    Item getItem();

    int getQuantity();

    /**
     * Determines if the stack is empty. This is different from Vanilla in that {@link VirtualStack#getItem()} doesn't get changed
     * to {@link Items#AIR}.
     *
     * @return The item
     */
    default boolean isEmpty() {
        return this.getQuantity() == 0;
    }

    int getMetadata();

    /**
     * Returns a copy of the {@link NBTTagCompound} or null if not present.
     *
     * @return The compound
     */
    @Nullable
    NBTTagCompound getCompound();

    /**
     * Returns a copy of this virtual stack, including NBT data (if present).
     *
     * @return The copy
     */
    VirtualStack copy();

    /**
     * Returns the current state of this virtual stack as an {@link ItemStack}.
     *
     * Note 1: The stack created may or may not have the NBT data associated with it.
     * Note 2: If the quantity falls to zero or less then this method will return a stack with
     *         a quantity of one to offset the fact that Vanilla will mark it as AIR.
     *         If you want to check empty status then use {@link VirtualStack#isEmpty()}.
     *
     * @return The stack
     */
    ItemStack asRealStack();
}
