/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

/**
 * How the items for sale are ordered, as 3.5's sort box: its translation key names it.
 */
public enum ExchangeSort {
    CREATED_ASC("almura.feature.common.text.sort.created_asc"),
    CREATED_DESC("almura.feature.common.text.sort.created_desc"),
    PRICE_ASC("almura.feature.common.text.sort.price_asc"),
    PRICE_DESC("almura.feature.common.text.sort.price_desc"),
    ITEM_NAME_ASC("almura.feature.common.text.sort.item_display_name_asc"),
    ITEM_NAME_DESC("almura.feature.common.text.sort.item_display_name_desc"),
    SELLER_ASC("almura.feature.exchange.text.sort.seller_name_asc"),
    SELLER_DESC("almura.feature.exchange.text.sort.seller_name_desc");

    private final String key;

    ExchangeSort(final String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

    public boolean ascending() {
        return this.name().endsWith("_ASC");
    }
}
