/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Exchange extends Serializable {

    Instant getCreated();

    String getId();

    String getName();

    UUID getCreator();

    String getPermission();

    boolean isHidden();

    Optional<Map<UUID, List<ListItem>>> getPendingItems();

    Optional<Map<UUID, List<ForSaleItem>>> getForSaleItems();
}
