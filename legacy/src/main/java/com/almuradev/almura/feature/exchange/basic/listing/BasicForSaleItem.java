/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.basic.listing;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.google.common.base.MoreObjects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;

import javax.annotation.Nullable;

public final class BasicForSaleItem implements ForSaleItem {

    private final BasicListItem listItem;
    private final Instant created;

    private int record;
    private BigDecimal price;

    private ItemStack cacheStack;

    public BasicForSaleItem(final BasicListItem listItem, final int record, final Instant created, final BigDecimal price) {
        checkNotNull(listItem);
        checkState(record >= 0, "Record must be greater than or equal to 0.");
        checkNotNull(price);
        checkState(price.doubleValue() >= 0, "Price must be greater than or equal to 0.");
        checkState(price.doubleValue() <= FeatureConstants.ONE_TRILLION, "Price must be lesser than or equal to 1 trillion.");

        this.listItem = listItem;
        this.record = record;
        this.created = created;
        this.price = price;

        listItem.setForSaleItem(this);
    }

    @Override
    public int getRecord() {
        return this.record;
    }

    public void setRecord(final Integer record) {
        this.record = record;
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

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    @Override
    public void setCompound(@Nullable NBTTagCompound compound) {
        this.listItem.setCompound(compound);
        this.cacheStack = null;
    }

    @Override
    public ForSaleItem copy() {
        return new BasicForSaleItem(this.listItem.copy(), this.record, this.created, this.price);
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
            .add("record", this.record)
            .add("created", this.created)
            .add("price", this.price)
            .toString();
    }
}
