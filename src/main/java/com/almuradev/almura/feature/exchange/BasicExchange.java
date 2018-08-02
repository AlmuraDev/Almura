/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.shared.feature.store.Store;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.google.common.base.MoreObjects;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class BasicExchange implements Exchange, Serializable {

    private final String id, name, permission;
    private final Instant created;
    private final UUID creator;
    private final boolean isHidden;

    @Nullable private String creatorName;

    @Nullable private transient Map<UUID, List<ListItem>> listings;
    @Nullable private transient Map<UUID, List<ForSaleItem>> forSaleItems;

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

    @Override
    public Optional<String> getCreatorName() {
        return Optional.ofNullable(this.creatorName);
    }

    public String getPermission() {
        return this.permission;
    }

    @Override
    public Optional<Map<UUID, List<ListItem>>> getPendingItems() {
        return Optional.ofNullable(this.listings);
    }

    @Override
    public Optional<Map<UUID, List<ForSaleItem>>> getForSaleItems() {
        return Optional.ofNullable(this.forSaleItems);
    }

    @Override
    public boolean isHidden() {
        return this.isHidden;
    }

    public void refreshCreatorName() {
        if (this.creator != Store.ZERO && Sponge.getPlatform().getExecutionType().isServer()) {
            Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(this.creator)
                .ifPresent(user -> this.creatorName = user.getName());
        }
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
            .add("creatorName", this.creatorName)
            .add("name", this.name)
            .add("permission", this.permission)
            .add("isHidden", this.isHidden)
            .toString();
    }
}
