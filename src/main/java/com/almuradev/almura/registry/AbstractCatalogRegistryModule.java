/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCatalogRegistryModule<C extends CatalogType> implements AlternateCatalogRegistryModule<C> {

    private final Map<String, C> map;

    protected AbstractCatalogRegistryModule(final int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
    }

    @Override
    public void registerDefaults() {
    }

    @Override
    public final Optional<C> getById(final String id) {
        return Optional.ofNullable(this.map.get(this.withDomain(id)));
    }

    private String withDomain(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return id;
    }

    @Override
    public final Collection<C> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public final Map<String, C> provideCatalogMap() {
        return this.map;
    }

    public interface Additional<C extends CatalogType> extends AlternateCatalogRegistryModule<C>, AdditionalCatalogRegistryModule<C> {

        @Override
        default void registerAdditionalCatalog(final C catalog) {
            this.provideCatalogMap().put(catalog.getId().toLowerCase(Locale.ENGLISH), catalog);
        }
    }

    public static abstract class Mapped<C extends CatalogType, R> extends AbstractCatalogRegistryModule<C> {

        protected Mapped(final int initialCapacity) {
            super(initialCapacity);
        }

        protected void register(String id, final R real) {
            final String name = id;
            id = SpongeImplHooks.getModIdFromClass(real.getClass()) + ':' + id;
            this.provideCatalogMap().put(id, (C) real);
            ((IMixinSetCatalogTypeId) real).setId(id, name);
        }
    }
}
