/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store;

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Store extends Serializable {

    Instant getCreated();

    String getId();

    String getName();

    UUID getCreator();

    String getPermission();

    boolean isHidden();

    Optional<Map<UUID, List<ForSaleItem>>> getForSaleItems();
}
