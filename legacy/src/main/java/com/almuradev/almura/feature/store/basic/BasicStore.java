/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.basic;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.google.common.base.MoreObjects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

public final class BasicStore implements Store {

    private final String id, name, permission;
    private final Instant created;
    private final UUID creator;
    private final boolean isHidden;
    private final List<BuyingItem> buyingItems = new CopyOnWriteArrayList<>();
    private final List<SellingItem> sellingItems = new CopyOnWriteArrayList<>();

    @Nullable private String creatorName;
    private boolean loaded = false;

    public BasicStore(final String id, final Instant created, final UUID creator, final String name, final String permission, final boolean
        isHidden) {
        this.id = id;
        this.created = created;
        this.creator = creator;
        this.name = name;
        this.permission = permission;
        this.isHidden = isHidden;
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
    public Instant getCreated() {
        return this.created;
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
    public boolean isHidden() {
        return this.isHidden;
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
    public List<SellingItem> getSellingItems() {
        return this.sellingItems;
    }

    @Override
    public void putSellingItems(@Nullable List<SellingItem> sellingItems) {
        this.clearSellingItems();

        if (sellingItems != null) {
            this.sellingItems.addAll(sellingItems);
        }
    }

    @Override
    public void clearSellingItems() {
        this.sellingItems.clear();
    }

    @Override
    public List<BuyingItem> getBuyingItems() {
        return this.buyingItems;
    }

    @Override
    public void putBuyingItems(@Nullable List<BuyingItem> buyingItems) {
        this.clearBuyingItems();

        if (buyingItems != null) {
            this.buyingItems.addAll(buyingItems);
        }
    }

    @Override
    public void clearBuyingItems() {
        this.buyingItems.clear();
    }

    @Override
    public void setCreatorName(@Nullable String creatorName) {
        this.creatorName = creatorName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BasicStore other = (BasicStore) o;
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
