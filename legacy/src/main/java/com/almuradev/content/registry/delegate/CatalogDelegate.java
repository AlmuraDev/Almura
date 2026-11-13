/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry.delegate;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.DelegateNotSatisfiedException;
import com.almuradev.content.component.delegate.LazyDelegate;
import com.almuradev.content.registry.ResourceLocations;
import com.google.common.base.MoreObjects;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;

public final class CatalogDelegate<C extends CatalogType> extends LazyDelegate<C> {
    private final Class<C> type;
    private final String id;

    public static <C extends CatalogType> Delegate<C> create(final Class<C> type, final String id) {
        return new CatalogDelegate<>(type, id);
    }

    public static <C extends CatalogType> Delegate<C> namespaced(final Class<C> type, final ConfigurationNode config) {
        return namespaced(type, config.getString());
    }

    public static <C extends CatalogType> Delegate<C> namespaced(final Class<C> type, final String id) {
        return new CatalogDelegate<>(type, ResourceLocations.requireNamespaced(id));
    }

    private CatalogDelegate(final Class<C> type, final String id) {
        this.type = type;
        this.id = id;
    }

    @Nullable
    @Override
    protected C load() {
        return Sponge.getRegistry().getType(this.type, this.id).orElse(null);
    }

    @Override
    public C require() {
        if(this.value != null) {
            return this.value;
        }
        return this.optional().orElseThrow(() -> new DelegateNotSatisfiedException("Could not resolve '" + this.type.getName() + "' with id '" + this.id + '\''));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("type", this.type)
            .add("id", this.id)
            .toString();
    }
}
