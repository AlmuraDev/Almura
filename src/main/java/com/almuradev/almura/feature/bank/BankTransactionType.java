/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import org.jspecify.annotations.Nullable;

/**
 * An entry in an account's history, as 3.5's.
 */
public enum BankTransactionType {
    CREATE,
    RENAME,
    DELETE,
    DEPOSIT,
    WITHDRAW,
    COIN_DEPOSIT,
    // The balance paid back to the owner's wallet when an account is deleted
    CLOSING_PAYOUT,
    MEMBER_ADD,
    MEMBER_REMOVE,
    MEMBER_CHANGE;

    /**
     * @return "+" for money in, "-" for money out, null for the rest
     */
    public @Nullable String sign() {
        return switch (this) {
            case DEPOSIT, COIN_DEPOSIT -> "+";
            case WITHDRAW, CLOSING_PAYOUT -> "-";
            default -> null;
        };
    }
}
