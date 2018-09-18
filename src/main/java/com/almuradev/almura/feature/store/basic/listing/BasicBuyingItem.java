/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.basic.listing;

import com.almuradev.almura.feature.store.listing.BuyingItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.time.Instant;

import javax.annotation.Nullable;

public final class BasicBuyingItem extends AbstractStoreItem implements BuyingItem {

    public BasicBuyingItem(final int record, final Instant created, final Item item, final int metadata, final int quantity, final BigDecimal price,
        final int index, @Nullable final NBTTagCompound compound) {
        super(record, created, item, metadata, quantity, price, index, compound);
    }

    @Override
    public BuyingItem copy() {
        return new BasicBuyingItem(this.record, this.created, this.item, this.metadata, this.quantity, this.price, this.index, this.compound);
    }
}
