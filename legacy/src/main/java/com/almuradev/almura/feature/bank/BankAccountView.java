/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A bank account as one player sees it in the GUI: its balance and what they may do with it.
 */
public final class BankAccountView {

    private final int recNo;
    private final String name, ownerName;
    // The player is the owner (rename, delete, members); may withdraw (owners always)
    private final boolean mine, canWithdraw;
    private final BigDecimal balance;
    private final List<Member> members;

    public BankAccountView(final int recNo, final String name, final String ownerName, final boolean mine, final boolean canWithdraw,
        final BigDecimal balance, final List<Member> members) {
        this.recNo = recNo;
        this.name = name;
        this.ownerName = ownerName;
        this.mine = mine;
        this.canWithdraw = canWithdraw;
        this.balance = balance;
        this.members = members;
    }

    public int getRecNo() {
        return this.recNo;
    }

    public String getName() {
        return this.name;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public boolean isMine() {
        return this.mine;
    }

    public boolean canWithdraw() {
        return this.canWithdraw;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public boolean isShared() {
        return !this.members.isEmpty();
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(this.members);
    }

    public static final class Member {

        private final UUID uniqueId;
        private final String name;
        private final boolean canWithdraw;

        public Member(final UUID uniqueId, final String name, final boolean canWithdraw) {
            this.uniqueId = uniqueId;
            this.name = name;
            this.canWithdraw = canWithdraw;
        }

        public UUID getUniqueId() {
            return this.uniqueId;
        }

        public String getName() {
            return this.name;
        }

        public boolean canWithdraw() {
            return this.canWithdraw;
        }
    }
}
