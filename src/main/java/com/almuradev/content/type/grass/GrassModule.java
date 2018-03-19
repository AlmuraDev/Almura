/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.grass.processor.BlockProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class GrassModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("grass", GrassContentTypeLoader.class));
        this.facet().add(GrassContentTypeLoader.class);
        this.bind(Grass.Builder.class).to(GrassBuilder.class);
        this.registry().module(Grass.class, GrassRegistryModule.class);
        this.processors().add(BlockProcessor.class);
    }

    private SingleTypeProcessorBinder<Grass, Grass.Builder, ConfigProcessor<? extends Grass.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends Grass.Builder>>() {});
    }
}
