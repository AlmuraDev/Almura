/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A bank account, as the server knows it. Its money is held by the economy plugin in the virtual account {@link #getEconomyId()}; Almura
 * keeps its owner, name and members.
 */
public final class BankAccount {

    private final int recNo;
    private final Instant created;
    private final UUID owner;
    private final String name;
    private final String economyId;
    // Members and whether each may withdraw (everyone on the account may deposit)
    private final Map<UUID, Boolean> members;

    public BankAccount(final int recNo, final Instant created, final UUID owner, final String name, final String economyId,
        final Map<UUID, Boolean> members) {
        this.recNo = recNo;
        this.created = created;
        this.owner = owner;
        this.name = name;
        this.economyId = economyId;
        this.members = new LinkedHashMap<>(members);
    }

    public int getRecNo() {
        return this.recNo;
    }

    public Instant getCreated() {
        return this.created;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public String getEconomyId() {
        return this.economyId;
    }

    public Map<UUID, Boolean> getMembers() {
        return Collections.unmodifiableMap(this.members);
    }

    public boolean isOwner(final UUID player) {
        return this.owner.equals(player);
    }

    public boolean isMember(final UUID player) {
        return this.members.containsKey(player);
    }

    public boolean canAccess(final UUID player) {
        return this.isOwner(player) || this.isMember(player);
    }

    public boolean canWithdraw(final UUID player) {
        return this.isOwner(player) || this.members.getOrDefault(player, false);
    }
}
