/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * What each coin is worth, by item id (the Coin Exchange block and the bank's coin deposits).
 */
public final class BankCoins {

    private static final Map<String, Long> VALUES = ImmutableMap.of(
        "almura:normal/currency/coppercoin", 100L,
        "almura:normal/currency/silvercoin", 1_000L,
        "almura:normal/currency/goldcoin", 100_000L,
        "almura:normal/currency/platinumcoin", 1_000_000L
    );

    private BankCoins() {
    }

    /**
     * @return What one of the item is worth, 0 if it isn't a coin
     */
    public static long valueOf(final String itemId) {
        return VALUES.getOrDefault(itemId, 0L);
    }
}
