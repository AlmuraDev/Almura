/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing;

import com.almuradev.almura.shared.item.DynamicCompoundStack;
import com.almuradev.almura.shared.item.VirtualStack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ListItem extends DynamicCompoundStack {

    int getRecord();

    Instant getCreated();

    UUID getSeller();

    BigDecimal getPrice();

    int getIndex();

    Collection<ForSaleItem> getListings();

    @Override
    ListItem copy();
}
