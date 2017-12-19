/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry.delegate;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.LazyDelegate;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;

public final class CatalogDelegate<C extends CatalogType> extends LazyDelegate<C> {

    private final Class<C> type;
    private final String id;

    public static <C extends CatalogType> Delegate<C> create(final Class<C> type, final String id) {
        return new CatalogDelegate<>(type, id);
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
}
