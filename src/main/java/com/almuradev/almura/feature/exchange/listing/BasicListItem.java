/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import com.almuradev.almura.feature.exchange.Exchange;
import com.google.common.base.MoreObjects;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class BasicListItem implements ListItem, Serializable {

    private final Exchange exchange;
    private final Instant created;
    private final UUID seller;
    private final ItemStackSnapshot item;
    private final BigDecimal price;
    private final int quantity;
    private final int index;

    private transient List<ForSaleItem> listed = new ArrayList<>();

    public BasicListItem(final Exchange exchange, final Instant created, final UUID seller, final ItemStackSnapshot item, final BigDecimal price,
        final int quantity, final int index) {
        this.exchange = exchange;
        this.created = created;
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.index = index;
    }

    @Override
    public Exchange getExchange() {
        return this.exchange;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    @Override
    public UUID getSeller() {
        return this.seller;
    }

    @Override
    public ItemStackSnapshot getItem() {
        return this.item;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public Collection<ForSaleItem> getListings() {
        return this.listed;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("exchange", this.exchange.getId())
            .add("created", this.created)
            .add("seller", this.seller)
            .add("item", this.item)
            .add("price", this.price)
            .add("quantity", this.quantity)
            .add("index", this.index)
            .toString();
    }
}
