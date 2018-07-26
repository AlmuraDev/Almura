/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import com.almuradev.almura.feature.exchange.Exchange;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ListItem {

    Exchange getExchange();

    Instant getCreated();

    UUID getSeller();

    ItemStackSnapshot getItem();

    BigDecimal getPrice();

    int getQuantity();

    int getIndex();

    Collection<ForSaleItem> getListings();
}
