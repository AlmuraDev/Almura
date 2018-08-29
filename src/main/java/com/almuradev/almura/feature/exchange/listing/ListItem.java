/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import com.almuradev.almura.shared.item.VanillaStack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

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

    void syncSellerNameToUniqueId();

    int getIndex();

    Optional<ForSaleItem> getForSaleItem();

    Optional<BigDecimal> getLastKnownPrice();

    @Override
    ListItem copy();

    /**
     * INTERNAL USE ONLY
     */
    void setSellerName(@Nullable final String sellerName);

    /**
     * INTERNAL USE ONLY
     */
    void setForSaleItem(@Nullable final ForSaleItem forSaleItem);

    /**
     * INTERNAL USE ONLY
     */
    void setLastKnownPrice(@Nullable final BigDecimal lastKnownPrice);
}
