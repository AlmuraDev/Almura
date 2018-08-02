/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store;

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface Store extends Serializable {

    UUID UNKNOWN_OWNER = new UUID(0, 0);

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

    Map<UUID, List<ForSaleItem>> getForSaleItems();

    Optional<List<ForSaleItem>> getForSaleItemsFor(final UUID uuid);

    void putForSaleItems(@Nullable final Map<UUID, List<ForSaleItem>> forSaleItems);

    void putForSaleItemsFor(final UUID uuid, @Nullable final List<ForSaleItem> forSaleItems);

    void clearForSaleItems();

    /**
     * Returns if the store is currently in a dirty state. This can be defined as the store needing to save data to the database but isn't the
     * strictest definition of this.
     *
     * @return True if dirty, false if not
     */
    boolean isDirty();

    void setDirty(final boolean dirty);
}
