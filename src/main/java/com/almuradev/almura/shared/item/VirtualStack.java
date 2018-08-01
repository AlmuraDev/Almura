/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

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

    int getMetadata();

    /**
     * Returns a copy of the {@link NBTTagCompound} or null if not present.
     *
     * @return The compound
     */
    @Nullable
    NBTTagCompound getCompound();

    /**
     * Sets the {@link NBTTagCompound}. The given compound will be copied if not null./
     *
     * @param compound The compound
     */
    void setCompound(@Nullable final NBTTagCompound compound);

    /**
     * Returns a copy of this virtual stack, including NBT data (if present).
     *
     * @return The copy
     */
    VirtualStack copy();

    /**
     * Returns the current state of this virtual stack as an {@link ItemStack}.
     *
     * Note: The stack created may or may not have the NBT data associated with it.
     *
     * @return The stack
     */
    ItemStack asRealStack();
}
