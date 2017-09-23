/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.registry.binder;

import com.google.inject.Injector;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistryModule;

/**
 * An abstract registry binder entry for a registry module
 *
 * @param <T> the type
 */
abstract class ModuleEntry<T> extends AbstractEntry {

    /**
     * A registry binder entry for a non-catalog registry module that will be
     * constructed through the injector.
     *
     * @param <T> the type
     */
    static class Provider<T> extends ModuleEntry<T> {

        private final Class<? extends RegistryModule> module;

        Provider(final Class<? extends RegistryModule> module) {
            this.module = module;
        }

        @Override
        public void install(final Injector injector, final GameRegistry registry) {
            registry.registerModule(injector.getInstance(this.module));
        }
    }

    /**
     * A registry binder entry for a catalog registry module that will be
     * constructed through the injector.
     *
     * @param <C> the catalog type
     */
    static final class CatalogProvider<C extends CatalogType> extends ModuleEntry<C> {

        private final Class<C> type;
        private final Class<? extends CatalogRegistryModule<C>> module;

        CatalogProvider(final Class<C> type, final Class<? extends CatalogRegistryModule<C>> module) {
            this.type = type;
            this.module = module;
        }

        @Override
        public void install(final Injector injector, final GameRegistry registry) {
            registry.registerModule(this.type, injector.getInstance(this.module));
        }
    }

    /**
     * A registry binder entry for an already constructed catalog registry module
     * that will be registered with the registry.
     *
     * @param <C> the catalog type
     */
    static final class CatalogInstance<C extends CatalogType> extends ModuleEntry<C> {

        private final RegistryModule module;

        CatalogInstance(final RegistryModule module) {
            this.module = module;
        }

        @Override
        public void install(final Injector injector, final GameRegistry registry) {
            registry.registerModule(this.module);
        }
    }

    /**
     * A registry binder entry for an already constructed dummy-enabled catalog registry module
     * that will be registered with the registry.
     *
     * @param <C> the catalog type
     */
    static final class DummyEnabledCatalogInstance<C extends CatalogType> extends ModuleEntry<C> {

        private final Class<C> type;
        private final CatalogRegistryModule<C> module;

        DummyEnabledCatalogInstance(final Class<C> type, final CatalogRegistryModule<C> module) {
            this.type = type;
            this.module = module;
        }

        @Override
        public void install(final Injector injector, final GameRegistry registry) {
            registry.registerModule(this.type, this.module);
        }
    }
}
