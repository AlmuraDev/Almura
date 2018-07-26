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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

public final class BasicForSaleItem implements ForSaleItem {
    private final ListItem listItem;
    private final Instant created;
    private int quantity;
    private transient Collection<Transaction> transactions = new ArrayList<>();

    public BasicForSaleItem(final ListItem listItem, final Instant created, final int quantity) {
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("listItem", this.listItem)
            .add("created", this.created)
            .add("quantity", this.quantity)
            .toString();
    }
}
