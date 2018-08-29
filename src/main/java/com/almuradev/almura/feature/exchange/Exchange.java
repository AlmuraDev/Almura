/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.shared.feature.IngameFeature;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface Exchange extends IngameFeature {

    Map<UUID, List<ListItem>> getListItems();

    Optional<List<ListItem>> getListItemsFor(final UUID uuid);

    void putListItems(@Nullable final Map<UUID, List<ListItem>> listItems);

    void putListItemsFor(final UUID uuid, @Nullable final List<ListItem> listItems);

    void clearListItems();

    Map<UUID, List<ForSaleItem>> getForSaleItems();

    Optional<List<ForSaleItem>> getForSaleItemsFor(final UUID uuid);

    void putForSaleItems(@Nullable final Map<UUID, List<ForSaleItem>> forSaleItems);

    void putForSaleItemsFor(final UUID uuid, @Nullable final List<ForSaleItem> forSaleItems);

    void clearForSaleItems();
}
