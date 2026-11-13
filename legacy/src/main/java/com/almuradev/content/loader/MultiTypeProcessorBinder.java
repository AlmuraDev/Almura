/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.ContentType;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;

public final class MultiTypeProcessorBinder<T extends ContentType.MultiType<C, B>, C extends CatalogedContent, B extends ContentBuilder<C>, P extends ConfigProcessor<? extends B>> {
    private final T[] types;
    private final MapBinder<T, P> processors;

    public MultiTypeProcessorBinder(final Binder binder, final T[] types, final TypeLiteral<T> type, final TypeLiteral<P> processor) {
        this.types = types;
        this.processors = MapBinder.newMapBinder(
                binder,
                new FriendlyTypeLiteral<T>() {}.where(new TypeArgument<T>(type) {}),
                new FriendlyTypeLiteral<P>() {}.where(new TypeArgument<P>(processor) {})
        ).permitDuplicates();
    }

    public final MultiTypeProcessorBinder<T, C, B, P> all(final Class<? extends ConfigProcessor<?>> processor) {
        for (final T type : this.types) {
            this.processors.addBinding(type).to((Class<? extends P>) processor);
        }
        return this;
    }

    @SafeVarargs
    public final MultiTypeProcessorBinder<T, C, B, P> only(final Class<? extends ConfigProcessor<?>> processor, final T... types) {
        for (final T type : types) {
            this.processors.addBinding(type).to((Class<? extends P>) processor);
        }
        return this;
    }
}
