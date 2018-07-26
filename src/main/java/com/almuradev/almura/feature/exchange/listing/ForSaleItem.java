/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.listing;

import java.time.Instant;
import java.util.Collection;

public interface ForSaleItem {

    ListItem getListItem();

    Instant getCreated();

    int getQuantity();

    Collection<Transaction> getTransactions();
}
