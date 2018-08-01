/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.Store;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Exchange extends Store {

    Optional<Map<UUID, List<ListItem>>> getPendingItems();
}
