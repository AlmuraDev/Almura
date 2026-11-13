/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.shared.feature.IngameFeature;

import java.util.List;

import javax.annotation.Nullable;

public interface Store extends IngameFeature {

    List<BuyingItem> getBuyingItems();

    void putBuyingItems(@Nullable final List<BuyingItem> buyingItems);

    void clearBuyingItems();

    List<SellingItem> getSellingItems();

    void putSellingItems(@Nullable final List<SellingItem> sellingItems);

    void clearSellingItems();
}
