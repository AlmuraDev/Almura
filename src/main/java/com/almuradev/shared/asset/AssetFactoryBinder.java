/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.asset;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetPipeline;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.loader.StageTask;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An asset binder.
 */
public class AssetFactoryBinder {

    private final Multibinder<Entry<? extends StageTask<?, ?>>> binder;

    public static AssetFactoryBinder create(final Binder binder) {
        return new AssetFactoryBinder(binder);
    }

    private AssetFactoryBinder(final Binder binder) {
        this.binder = Multibinder.newSetBinder(binder, new TypeLiteral<Entry<? extends StageTask<?, ?>>>() {});
    }

    /**
     * Creates a binding for the specified factory provider.
     *
     * @param provider the provider class
     * @param consumer the consumer
     * @param <F> the factory type
     * @return this binder
     */
    public <F extends StageTask<?, ?>> AssetFactoryBinder provider(final Class<F> provider, final Consumer<Entry<F>> consumer) {
        final Entry<F> entry = new Entry<>(provider);
        consumer.accept(entry);
        this.binder.addBinding().toInstance(entry);
        return this;
    }

    /**
     * An asset factory binding entry.
     *
     * @param <F> the factory type
     */
    public static class Entry<F extends StageTask> {

        private final Class<F> provider;
        private LoaderPhase phase;
        private final Set<Asset.Type> types = EnumSet.noneOf(Asset.Type.class);

        Entry(final Class<F> provider) {
            this.provider = provider;
        }

        /**
         * Sets the phase.
         *
         * @param phase the phase
         */
        public void phase(final LoaderPhase phase) {
            this.phase = phase;
        }

        /**
         * Sets the asset types the factory applies to.
         *
         * @param types the asset types
         */
        public void type(final Asset.Type... types) {
            Collections.addAll(this.types, types);
        }

        @SuppressWarnings("unchecked")
        void install(final AssetPipeline pipeline, final Injector injector) {
            pipeline.registerStage(this.phase, injector.getInstance(this.provider), this.types);
        }
    }
}
