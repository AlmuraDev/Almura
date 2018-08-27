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

import com.almuradev.almura.shared.feature.store.Store;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class BasicListItem implements ListItem {

    private int record;
    private final Instant created;
    private final UUID seller;
    private final Item item;
    private final int metadata, index;

    private int quantity;
    @Nullable private String sellerName;
    @Nullable private ItemStack cacheStack;
    @Nullable NBTTagCompound compound;
    @Nullable private ForSaleItem forSaleItem;
    @Nullable private BigDecimal lastKnownPrice;

    public BasicListItem(final int record, final Instant created, final UUID seller, final Item item, final int quantity, final int metadata,
        final int index, @Nullable final NBTTagCompound compound) {
        checkNotNull(created);
        checkNotNull(seller);
        checkNotNull(item);
        checkState(quantity > 0, "Quantity must be greater than 0.");
        checkState(metadata >= 0, "Metadata must be greater than or equal to 0.");
        checkState(index >= 0, "Index must be greater than or equal to 0.");

        this.record = record;
        this.created = created;
        this.seller = seller;
        this.item = item;
        this.quantity = quantity;
        this.metadata = metadata;
        this.index = index;
        this.compound = compound;
    }

    @Override
    public int getRecord() {
        return this.record;
    }

    public void setRecord(final Integer record) {
        this.record = record;
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
    public Optional<String> getSellerName() {
        return Optional.ofNullable(this.sellerName);
    }

    @Override
    public void setSellerName(@Nullable final String sellerName) {
        this.sellerName = sellerName;
    }

    @Override
    public void setForSaleItem(@Nullable ForSaleItem forSaleItem) {
        this.forSaleItem = forSaleItem;
    }

    @Override
    public void syncSellerNameToUniqueId() {
        if (this.seller != Store.UNKNOWN_OWNER) {
            Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(this.seller)
              .ifPresent(user -> this.sellerName = user.getName());
        }
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
        if (this.compound == null) {
            return null;
        }

        return this.compound.copy();
    }

    @Override
    public void setCompound(@Nullable NBTTagCompound compound) {
        this.compound = compound == null ? null : compound.copy();
        this.cacheStack = null;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public Optional<ForSaleItem> getForSaleItem() {
        return Optional.ofNullable(this.forSaleItem);
    }

    @Override
    public Optional<BigDecimal> getLastKnownPrice() {
        return Optional.ofNullable(this.lastKnownPrice);
    }

    @Override
    public void setLastKnownPrice(@Nullable BigDecimal lastKnownPrice) {
        this.lastKnownPrice = lastKnownPrice;
    }

    @Override
    public BasicListItem copy() {
        return new BasicListItem(this.record, this.created, this.seller, this.item, this.quantity, this.metadata, this.index,
          this.compound == null ? null : this.compound.copy());
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
            .add("seller", this.seller)
            .add("sellerName", this.sellerName)
            .add("item", this.item.getRegistryName())
            .add("quantity", this.quantity)
            .add("metadata", this.metadata)
            .add("index", this.index)
            .add("compound", this.compound)
            .toString();
    }
}
