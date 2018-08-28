/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing.basic;

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.Transaction;
import com.google.common.base.MoreObjects;

import java.time.Instant;
import java.util.UUID;

public final class BasicTransaction implements Transaction {

    private final ForSaleItem forSaleItem;
    private final Instant created;
    private final UUID buyer;
    private final int quantity;

    public BasicTransaction(final ForSaleItem forSaleItem, final Instant created, final UUID buyer, final int quantity) {
        this.forSaleItem = forSaleItem;
        this.created = created;
        this.buyer = buyer;
        this.quantity = quantity;
    }

    @Override
    public ForSaleItem getItemForSale() {
        return this.forSaleItem;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    @Override
    public UUID getBuyer() {
        return this.buyer;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("item", this.forSaleItem)
            .add("created", this.created)
            .add("buyer", this.buyer)
            .add("quantity", this.quantity)
            .toString();
    }
}
