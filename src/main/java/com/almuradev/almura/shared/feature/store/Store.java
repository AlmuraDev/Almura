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

    UUID ZERO = new UUID(0, 0);

    Instant getCreated();

    String getId();

    String getName();

    UUID getCreator();

    /**
     * Returns the creator name, if known. A server shop who is created by default would not have one.
     *
     * No assumption should ever be made that the name is a direct correlation to the UUID.
     *
     * @return The name, if known
     */
    Optional<String> getCreatorName();

    String getPermission();

    boolean isHidden();

    Optional<Map<UUID, List<ForSaleItem>>> getForSaleItems();
}
