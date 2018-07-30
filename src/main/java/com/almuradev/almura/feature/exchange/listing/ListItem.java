/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ListItem extends VirtualStack {

    int getRecord();

    Instant getCreated();

    UUID getSeller();

    BigDecimal getPrice();

    int getIndex();

    Collection<ForSaleItem> getListings();

    @Override
    ListItem copy();
}
