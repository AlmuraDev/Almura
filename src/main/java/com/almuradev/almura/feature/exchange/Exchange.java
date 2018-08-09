/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.shared.feature.store.Store;
import com.almuradev.almura.shared.feature.store.listing.ListItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface Exchange extends Store {

    Map<UUID, List<ListItem>> getListItems();

    Optional<List<ListItem>> getListItemsFor(final UUID uuid);

    void putListItems(@Nullable final Map<UUID, List<ListItem>> listItems);

    void putListItemsFor(final UUID uuid, @Nullable final List<ListItem> listItems);

    void clearListItems();
}
