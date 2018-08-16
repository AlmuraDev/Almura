/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing;

import com.almuradev.almura.shared.item.DynamicCompoundStack;
import com.almuradev.almura.shared.item.VirtualStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

import javax.annotation.Nullable;

public interface ForSaleItem extends DynamicCompoundStack {

    int getRecord();

    ListItem getListItem();

    Instant getCreated();

    BigDecimal getPrice();

    Collection<Transaction> getTransactions();

    @Override
    default Item getItem() {
        return this.getListItem().getItem();
    }

    default int getQuantityRemaining() {
        return this.getQuantity();
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
