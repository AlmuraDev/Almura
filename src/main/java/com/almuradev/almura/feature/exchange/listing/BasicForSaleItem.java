/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import net.minecraft.item.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

public final class BasicForSaleItem implements ForSaleItem {

    private final BasicListItem listItem;
    private final Instant created;
    private final int quantity;
    private final Collection<Transaction> transactions = new ArrayList<>();

    public BasicForSaleItem(final BasicListItem listItem, final Instant created, final int quantity) {
        checkState(listItem.getQuantity() >= quantity);

        this.listItem = listItem;
        this.created = created;
        this.quantity = quantity;
    }

    @Override
    public ListItem getListItem() {
        return this.listItem;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public Collection<Transaction> getTransactions() {
        return this.transactions;
    }

    @Override
    public ForSaleItem copy() {
        return new BasicForSaleItem(this.listItem.copy(), this.created, this.quantity);
    }

    @Override
    public ItemStack asRealStack() {
        final ItemStack stack = new ItemStack(this.getItem(), this.quantity, this.getMetadata());
        if (this.listItem.compound != null) {
            stack.setTagCompound(this.listItem.compound.copy());
        }

        return stack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("item", this.listItem)
            .add("created", this.created)
            .add("quantity", this.quantity)
            .toString();
    }
}
