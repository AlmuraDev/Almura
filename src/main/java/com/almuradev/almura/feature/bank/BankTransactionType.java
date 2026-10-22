/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

/**
 * What a bank history entry records.
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
    MEMBER_CHANGE
}
