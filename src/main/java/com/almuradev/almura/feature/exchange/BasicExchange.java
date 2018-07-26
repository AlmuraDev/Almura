/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class BasicExchange implements Exchange, Serializable {

    private final String id, name, permission;
    private final Instant created;
    private final UUID creator;
    private final boolean isHidden;
    private transient Map<UUID, List<ListItem>> listings = new HashMap<>();
    private transient Map<UUID, List<ForSaleItem>> forSaleItems = new HashMap<>();

    public BasicExchange(final String id, final Instant created, final UUID creator, final String name, final String permission, final boolean
        isHidden) {
        this.id = id;
        this.created = created;
        this.creator = creator;
        this.name = name;
        this.permission = permission;
        this.isHidden = isHidden;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public UUID getCreator() {
        return this.creator;
    }

    public String getPermission() {
        return this.permission;
    }

    @Override
    public Optional<Map<UUID, List<ListItem>>> getPendingItems() {
        return Optional.empty();
    }

    @Override
    public Optional<Map<UUID, List<ForSaleItem>>> getForSaleItems() {
        return Optional.empty();
    }

    @Override
    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BasicExchange other = (BasicExchange) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", this.id)
            .add("created", this.created)
            .add("creator", this.creator)
            .add("name", this.name)
            .add("permission", this.permission)
            .add("isHidden", this.isHidden)
            .toString();
    }
}
