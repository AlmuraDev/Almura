/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import java.util.Map;

/**
 * What Almura's coins are worth, as 3.5's.
 */
public final class BankCoins {

    private static final Map<String, Long> VALUES = Map.of(
        "almura:normal/currency/coppercoin", 100L,
        "almura:normal/currency/silvercoin", 1_000L,
        "almura:normal/currency/goldcoin", 100_000L,
        "almura:normal/currency/platinumcoin", 1_000_000L
    );

    private BankCoins() {
    }

    /**
     * @return what one of the item is worth, 0 if it isn't a coin
     */
    public static long valueOf(final String itemId) {
        return VALUES.getOrDefault(itemId, 0L);
    }
}
