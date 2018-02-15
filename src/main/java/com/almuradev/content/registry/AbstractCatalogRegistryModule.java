/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCatalogRegistryModule<C extends CatalogType> implements AlternateCatalogRegistryModule<C> {

    private final boolean eager = this.getClass().isAnnotationPresent(EagerCatalogRegistration.class);
    private final Map<String, C> map;

    protected AbstractCatalogRegistryModule(final int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
        this.register(true);
    }

    /** @deprecated do not call - internal use only */
    @CustomCatalogRegistration
    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Deprecated
    public final void sponge$registry() {
        this.register(false);
    }

    private void register(final boolean eager) {
        if (eager != this.eager) {
            return;
        }
        this.registerDefaults();
    }

    @Override
    public final Optional<C> getById(final String id) {
        return Optional.ofNullable(this.map.get(ResourceLocations.defaultedNamespace(ResourceLocations.toLowerCase(id))));
    }

    @Override
    public final Collection<C> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public final Map<String, C> provideCatalogMap() {
        return this.map;
    }

    public static abstract class Mapped<C extends CatalogType, R> extends AbstractCatalogRegistryModule<C> {

        protected Mapped(final int initialCapacity) {
            super(initialCapacity);
        }

        protected final void register(String id, final R real) {
            id = ResourceLocations.toLowerCase(id);
            final String name = id;
            if (id.indexOf(':') == -1) {
                id = SpongeImplHooks.getModIdFromClass(real.getClass()) + ':' + id;
            }
            this.provideCatalogMap().put(id, (C) real);
            ((IMixinSetCatalogTypeId) real).setId(id, name);
        }

        // soft implementation
        public final void registerAdditionalCatalog(final C catalog) {
            this.register(catalog.getId(), (R) catalog);
        }
    }
}
