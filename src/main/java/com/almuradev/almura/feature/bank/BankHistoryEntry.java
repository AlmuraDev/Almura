/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One line of a bank account's history, as shown in the GUI.
 */
public final class BankHistoryEntry {

    private final Instant created;
    private final String accountName, actorName;
    private final BankTransactionType type;
    private final BigDecimal amount;

    public BankHistoryEntry(final Instant created, final String accountName, final String actorName, final BankTransactionType type,
        final BigDecimal amount) {
        this.created = created;
        this.accountName = accountName;
        this.actorName = actorName;
        this.type = type;
        this.amount = amount;
    }

    public Instant getCreated() {
        return this.created;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getActorName() {
        return this.actorName;
    }

    public BankTransactionType getType() {
        return this.type;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }
}
