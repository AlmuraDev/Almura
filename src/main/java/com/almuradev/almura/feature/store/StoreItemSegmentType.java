package com.almuradev.almura.feature.store;

import com.almuradev.almura.feature.store.basic.listing.AbstractStoreItem;
import com.almuradev.almura.feature.store.basic.listing.BasicBuyingItem;
import com.almuradev.almura.feature.store.basic.listing.BasicSellingItem;

public enum StoreItemSegmentType {
    SELLING(BasicSellingItem.class),
    BUYING(BasicBuyingItem.class);

    private final Class<? extends AbstractStoreItem> clazz;

    StoreItemSegmentType(final Class<? extends AbstractStoreItem> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends AbstractStoreItem> getItemClass() {
        return this.clazz;
    }
}
