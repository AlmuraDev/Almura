/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.time.Instant;
import java.util.Collection;

import javax.annotation.Nullable;

public interface ForSaleItem extends VirtualStack {

    ListItem getListItem();

    Instant getCreated();

    int getQuantity();

    Collection<Transaction> getTransactions();

    @Override
    default Item getItem() {
        return this.getListItem().getItem();
    }

    @Override
    default int getMetadata() {
        return this.getListItem().getMetadata();
    }

    @Nullable
    @Override
    default NBTTagCompound getCompound() {
        return this.getListItem().getCompound();
    }

    @Override
    default void setCompound(@Nullable final NBTTagCompound compound) {
        this.getListItem().setCompound(compound);
    }

    @Override
    ForSaleItem copy();

    /**
     * Returns the current state of this selling listing as an {@link ItemStack}.
     *
     * Note: The stack created may or may not have the NBT data associated with it. The quantity returned will be the quantity of the selling listing,
     * not the quantity of the original listing
     *
     * @return The stack
     */
    @Override
    ItemStack asRealStack();
}