/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;

public final class SingleTypeProcessorBinder<C extends CatalogedContent, B extends ContentBuilder<C>, P extends ConfigProcessor<? extends B>> {
    private final Multibinder<P> processors;

    public SingleTypeProcessorBinder(final Binder binder, final TypeLiteral<P> processor) {
        this.processors = Multibinder.newSetBinder(
                binder,
                new FriendlyTypeLiteral<P>() {}.where(new TypeArgument<P>(processor) {})
        );
    }

    public SingleTypeProcessorBinder<C, B, P> add(final Class<? extends P> processor) {
        this.processors.addBinding().to(processor);
        return this;
    }
}
