/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.google.common.base.MoreObjects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public final class BasicExchange implements Exchange {

    private final String id, name, permission;
    private final Instant created;
    private final UUID creator;
    private final boolean isHidden;
    private final Map<UUID, List<ListItem>> listItems = new ConcurrentHashMap<>();
    private final Map<UUID, List<ForSaleItem>> forSaleItems = new ConcurrentHashMap<>();

    @Nullable private String creatorName;
    private boolean loaded = false;

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

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getCreator() {
        return this.creator;
    }

    @Override
    public Optional<String> getCreatorName() {
        return Optional.ofNullable(this.creatorName);
    }

    @Override
    public void syncCreatorNameToUniqueId() {
        if (this.creator != FeatureConstants.UNKNOWN_OWNER && Sponge.getPlatform().getExecutionType().isServer()) {
            Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(this.creator)
              .ifPresent(user -> this.creatorName = user.getName());
        }
    }

    @Override
    public String getPermission() {
        return this.permission;
    }

    @Override
    public Map<UUID, List<ListItem>> getListItems() {
        return this.listItems;
    }

    @Override
    public Optional<List<ListItem>> getListItemsFor(final UUID uuid) {
        checkNotNull(uuid);

        return Optional.ofNullable(this.listItems.get(uuid));
    }

    @Override
    public void putListItems(@Nullable final Map<UUID, List<ListItem>> listItems) {
        this.clearListItems();

        if (listItems != null) {
            this.listItems.putAll(listItems);
        }
    }

    @Override
    public void putListItemsFor(final UUID uuid, @Nullable final List<ListItem> listItems) {
        checkNotNull(uuid);

        this.listItems.remove(uuid);

        if (listItems != null) {
            this.listItems.put(uuid, listItems);
        }
    }

    @Override
    public void clearListItems() {
        this.listItems.clear();
    }

    @Override
    public Map<UUID, List<ForSaleItem>> getForSaleItems() {
        return this.forSaleItems;
    }

    @Override
    public Optional<List<ForSaleItem>> getForSaleItemsFor(final UUID uuid) {
        checkNotNull(uuid);

        return Optional.ofNullable(this.forSaleItems.get(uuid));
    }

    @Override
    public void putForSaleItems(@Nullable final Map<UUID, List<ForSaleItem>> forSaleItems) {
        this.clearForSaleItems();

        if (forSaleItems != null) {
            this.forSaleItems.putAll(forSaleItems);
        }
    }

    @Override
    public void putForSaleItemsFor(final UUID uuid, @Nullable final List<ForSaleItem> forSaleItems) {
        checkNotNull(uuid);

        this.forSaleItems.remove(uuid);

        if (forSaleItems != null) {
            this.forSaleItems.put(uuid, forSaleItems);
        }
    }

    @Override
    public void clearForSaleItems() {
        this.forSaleItems.clear();
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public void setCreatorName(@Nullable final String value) {
        this.creatorName = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BasicExchange other = (BasicExchange) o;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", this.id)
            .add("created", this.created)
            .add("creator", this.creator)
            .add("creatorName", this.creatorName)
            .add("name", this.name)
            .add("permission", this.permission)
            .add("isHidden", this.isHidden)
            .toString();
    }
}
