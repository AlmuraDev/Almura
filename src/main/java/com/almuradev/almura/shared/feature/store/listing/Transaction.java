/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.listing;

import java.time.Instant;
import java.util.UUID;

public interface Transaction {

    ForSaleItem getItemForSale();

    Instant getCreated();

    UUID getBuyer();

    int getQuantity();
}
