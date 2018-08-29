/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.basic.listing;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;

import javax.annotation.Nullable;

public abstract class AbstractStoreItem implements StoreItem {

    protected final Instant created;
    protected final Item item;
    protected final int record, metadata;

    protected int quantity, index;
    protected BigDecimal price;

    @Nullable protected ItemStack cacheStack;
    @Nullable protected NBTTagCompound compound;

    public AbstractStoreItem(final int record, final Instant created, final Item item, final int metadata, final int quantity, final BigDecimal price,
        final int index) {
        checkState(record >= 0);
        checkNotNull(created);
        checkNotNull(item);
        checkState(metadata >= 0);
        checkState(quantity >= 1);
        checkNotNull(price);
        checkState(index >= 0);

        this.record = record;
        this.created = created;
        this.item = item;
        this.metadata = metadata;
        this.quantity = quantity;
        this.price = price;
        this.index = index;
    }

    @Override
    public int getRecord() {
        return this.record;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setIndex(int index) {
        checkState(index >= 0);

        this.index = index;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public void setCompound(@Nullable final NBTTagCompound compound) {
        this.compound = compound == null ? null : compound.copy();
        this.cacheStack = null;
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
    public void setQuantity(final int quantity) {
        checkState(quantity >= FeatureConstants.UNLIMITED);

        this.quantity = quantity;
        this.cacheStack = null;
    }

    @Override
    public int getMetadata() {
        return this.metadata;
    }

    @Nullable
    @Override
    public NBTTagCompound getCompound() {
        return this.compound;
    }

    @Override
    public ItemStack asRealStack() {
        if (this.cacheStack == null) {
            this.cacheStack = new ItemStack(this.item, this.quantity, this.metadata);
            if (this.compound != null) {
                this.cacheStack.setTagCompound(this.compound.copy());
            }
        }

        return this.cacheStack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("record", this.record)
            .add("created", this.created)
            .add("item", this.item.getRegistryName())
            .add("quantity", this.quantity)
            .add("metadata", this.metadata)
            .add("index", this.index)
            .add("price", this.price)
            .add("compound", this.compound)
            .toString();
    }
}
