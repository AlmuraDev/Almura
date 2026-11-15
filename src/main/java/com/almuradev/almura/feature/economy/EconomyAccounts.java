/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.economy;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The server's economy accounts in its default currency, for Almura's features (banking): a player's wallet, or a named (virtual) account.
 * Used on the server's thread. Get it from {@link Economy#accounts()}.
 */
public interface EconomyAccounts {

    /**
     * An account: a player's ({@code player}), or a named one ({@code name}).
     */
    record Account(@Nullable UUID player, @Nullable String name) {

        public static Account player(final UUID player) {
            return new Account(player, null);
        }

        public static Account named(final String name) {
            return new Account(null, name);
        }
    }

    /**
     * @return the account's balance, or null if the economy has no such account and can't make it
     */
    @Nullable BigDecimal balance(Account account);

    /**
     * Moves money from one account to another.
     *
     * @return whether it moved
     */
    boolean transfer(Account from, Account to, BigDecimal amount);

    /**
     * Puts new money into an account (coins cashed in).
     *
     * @return whether it went in
     */
    boolean deposit(Account to, BigDecimal amount);

    /**
     * Takes money out of an account, out of the economy (bought from a store).
     *
     * @return whether it came out (not if the account doesn't have that much)
     */
    boolean withdraw(Account from, BigDecimal amount);
}
