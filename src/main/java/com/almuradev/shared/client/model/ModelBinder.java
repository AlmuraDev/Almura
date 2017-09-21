/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.client.model;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import net.kyori.membrane.facet.Enableable;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

/**
 * A model binder.
 */
@SideOnly(Side.CLIENT)
public final class ModelBinder {

    private final Multibinder<Entry<? extends ICustomModelLoader>> loaders;

    public static ModelBinder create(final Binder binder) {
        return new ModelBinder(binder);
    }

    private ModelBinder(final Binder binder) {
        this.loaders = Multibinder.newSetBinder(binder, new TypeLiteral<Entry<? extends ICustomModelLoader>>() {});
    }

    /**
     * Create a binding entry for the specified module type and class.
     *
     * @param loader the loader class
     * @param <L> the loader type
     * @return this binder
     */
    public <L extends ICustomModelLoader> ModelBinder loader(final Class<L> loader, final Consumer<Entry<L>> consumer) {
        final Entry<L> entry = new Entry<>(loader);
        consumer.accept(entry);
        this.loaders.addBinding().toInstance(entry);
        return this;
    }

    @SideOnly(Side.CLIENT)
    public static class Entry<L extends ICustomModelLoader> {

        private final Class<L> loader;
        private final Set<String> domains = new HashSet<>();

        Entry(final Class<L> loader) {
            this.loader = loader;
        }

        public void domains(final String... domains) {
            Collections.addAll(this.domains, domains);
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class Installer implements Enableable {

        private final Injector injector;
        private final Set<Entry<? extends ICustomModelLoader>> loaders;

        @Inject
        private Installer(final Injector injector, final Set<Entry<? extends ICustomModelLoader>> loaders) {
            this.injector = injector;
            this.loaders = loaders;
        }

        @Override
        public void enable() {
            this.loaders.forEach(entry -> {
                final ICustomModelLoader loader = this.injector.getInstance(entry.loader);
                ModelLoaderRegistry.registerLoader(loader);
                if (loader instanceof OnDemandModelLoader) {
                    entry.domains.forEach(((OnDemandModelLoader) loader)::registerInterest);
                }
            });
        }

        @Override
        public void disable() {
        }
    }
}
