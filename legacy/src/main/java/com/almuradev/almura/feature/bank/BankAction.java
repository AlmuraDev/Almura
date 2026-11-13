/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

/**
 * What a player asks the bank to do.
 */
public enum BankAction {
    // Open the bank window (the Almura Features menu)
    OPEN,
    // Refresh the accounts and balances
    REFRESH,
    CREATE,
    RENAME,
    DELETE,
    // Wallet to account
    DEPOSIT,
    // Account to wallet
    WITHDRAW,
    // The coins in the player's inventory, into an account or (account -1) their wallet
    DEPOSIT_COINS,
    ADD_MEMBER,
    REMOVE_MEMBER,
    SET_MEMBER_WITHDRAW,
    // An account's recent history
    HISTORY,
    // Admins: a player's history, by name
    ADMIN_HISTORY
}
