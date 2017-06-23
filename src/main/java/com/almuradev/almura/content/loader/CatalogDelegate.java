/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Due to the nature of how the game loads content, Almura cannot always guarantee that a {@link CatalogType} is currently registered.
 * That being said, we do know that it will be eventually. This class serves as a container of sorts that keeps a hold of the class the
 * {@link CatalogType} will be and the catalog's id. Finally, the {@link CatalogType} is lazy loaded when game logic requests the catalog.
 * @param <C> The type of the catalog
 */
public final class CatalogDelegate<C extends CatalogType> implements Predicate<C> {
    private final String catalogId;
    private final Class<C> catalogClass;
    @Nullable
    private C instance;

    public static <C extends CatalogType> CatalogDelegate<C> of(C instance) {
        final CatalogDelegate delegate = new CatalogDelegate(instance.getClass(), instance.getId());
        delegate.setCatalog(instance);
        return delegate;
    }

    public CatalogDelegate(Class<C> catalogClass, String catalogId) {
        this.catalogClass = catalogClass;
        this.catalogId = catalogId;
    }

    public C getCatalog() {
        @Nullable final C instance = this.getCatalog0();
        if (instance == null) {
            // TODO
            throw new IllegalStateException();
        }
        return instance;
    }

    @Nullable
    private C getCatalog0() {
        if (this.instance != null) {
            return this.instance;
        }
        this.instance = Sponge.getRegistry().getType(this.catalogClass, this.catalogId).orElse(null);
        return this.instance;
    }

    void setCatalog(C instance) {
        this.instance = instance;
    }

    @Override
    public boolean test(final C type) {
        return this.getCatalog().equals(type);
    }

    @Override
    public String toString() {
        final C instance = this.getCatalog0();
        return instance != null ? "SuppliedDelegate{delegate=" + instance + '}' : "EmptyDelegate{type=" + this.catalogClass + ", id=" + this.catalogId + '}';
    }
}
