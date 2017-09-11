/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import org.spongepowered.api.CatalogType;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Due to the nature of how the game loads content, Almura cannot always guarantee that a {@link CatalogType} is currently registered.
 * That being said, we do know that it will be eventually. This class serves as a container of sorts that keeps a hold of the class the
 * {@link CatalogType} will be and the catalog's id. Finally, the {@link CatalogType} is lazy loaded when game logic requests the catalog.
 * @param <C> The type of the catalog
 */
public final class CatalogDelegate<C extends CatalogType> implements Predicate<C>, Supplier<C> {

    private final Class<C> type;
    private final String id;
    @Nullable private C catalog;

    public static <C extends CatalogType> CatalogDelegate<C> of(final C catalog) {
        final CatalogDelegate<C> delegate = new CatalogDelegate<C>((Class<C>) catalog.getClass(), catalog.getId());
        delegate.catalog = catalog;
        return delegate;
    }

    public CatalogDelegate(final Class<C> type, final String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public C get() {
        @Nullable final C catalog = this.get0();
        if (catalog == null) {
            throw new IllegalStateException(String.format("Could not resolve a '%s' with an id of '%s'", this.type.getSimpleName(), this.id));
        }
        return catalog;
    }

    @Nullable
    private C get0() {
        if (this.catalog != null) {
            return this.catalog;
        }
        this.catalog = Registries.findCatalog(this.type, this.id).orElse(null);
        return this.catalog;
    }

    @Override
    public boolean test(final C catalog) {
        return this.get().equals(catalog);
    }

    @Override
    public String toString() {
        final C catalog = this.get0();
        return catalog != null ? "SuppliedDelegate{delegate=" + catalog + '}' : "EmptyDelegate{type=" + this.type + ", id=" + this.id + '}';
    }
}
