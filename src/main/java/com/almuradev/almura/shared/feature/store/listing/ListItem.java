/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing;

import com.almuradev.almura.shared.item.VanillaStack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ListItem extends VanillaStack {

    int getRecord();

    Instant getCreated();

    UUID getSeller();

    /**
     * Returns the seller name, if known.
     *
     * No assumption should ever be made that the name is a direct correlation to the UUID.
     *
     * @return The name, if known
     */
    Optional<String> getSellerName();

    BigDecimal getPrice();

    int getIndex();

    Collection<ForSaleItem> getListings();

    @Override
    ListItem copy();
}
