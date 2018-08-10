/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing.basic;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.Transaction;
import com.google.common.base.MoreObjects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

public final class BasicForSaleItem implements ForSaleItem {

    private final BasicListItem listItem;
    private final Instant created;
    private final BigDecimal price;
    private final Collection<Transaction> transactions = new ArrayList<>();

    private ItemStack cacheStack;

    public BasicForSaleItem(final BasicListItem listItem, final Instant created, final BigDecimal price) {
        checkNotNull(listItem);
        checkNotNull(price);

        this.listItem = listItem;
        this.created = created;
        this.price = price;
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
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public void setCompound(@Nullable NBTTagCompound compound) {
        this.listItem.setCompound(compound);
        this.cacheStack = null;
    }

    @Override
    public Collection<Transaction> getTransactions() {
        return this.transactions;
    }

    @Override
    public ForSaleItem copy() {
        return new BasicForSaleItem(this.listItem.copy(), this.created, this.price);
    }

    @Override
    public ItemStack asRealStack() {
        if (this.cacheStack == null) {
            this.cacheStack = new ItemStack(this.getItem(), this.getQuantity(), this.getMetadata());
            if (this.listItem.compound != null) {
                this.cacheStack.setTagCompound(this.listItem.compound.copy());
            }
        }

        return this.cacheStack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("listItem", this.listItem)
            .add("created", this.created)
            .add("price", this.price)
            .toString();
    }
}
