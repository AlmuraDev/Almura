/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public final class BasicListItem implements ListItem {

    private final Instant created;
    private final UUID seller;
    private final Item item;
    private final BigDecimal price;
    private final int metadata, quantity, index;
    private final List<ForSaleItem> listed = new ArrayList<>();

    @Nullable protected NBTTagCompound compound;

    public BasicListItem(final Instant created, final UUID seller, final Item item, final int quantity, final int metadata,
        final BigDecimal price, final int index) {
        this.created = created;
        this.seller = seller;
        this.item = item;
        this.quantity = quantity;
        this.metadata = metadata;
        this.price = price;
        this.index = index;
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
    public Item getItem() {
        return this.item;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public int getMetadata() {
        return this.metadata;
    }

    @Nullable
    @Override
    public NBTTagCompound getCompound() {
        if (this.compound == null) {
            return null;
        }

        return this.compound.copy();
    }

    @Override
    public void setCompound(@Nullable NBTTagCompound compound) {
        this.compound = compound == null ? null : compound.copy();
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
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
    public BasicListItem copy() {
        return new BasicListItem(this.created, this.seller, this.item, this.quantity, this.metadata, this.price, this.index);
    }

    @Override
    public ItemStack asRealStack() {
        final ItemStack stack = new ItemStack(this.item, this.quantity, this.metadata);
        if (this.compound != null) {
            stack.setTagCompound(this.compound.copy());
        }

        return stack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("created", this.created)
            .add("seller", this.seller)
            .add("item", this.item)
            .add("quantity", this.quantity)
            .add("metadata", this.metadata)
            .add("price", this.price)
            .add("index", this.index)
            .add("compound", this.compound)
            .toString();
    }
}
