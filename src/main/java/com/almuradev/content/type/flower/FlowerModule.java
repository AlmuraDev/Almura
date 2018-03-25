/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.flower;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.flower.processor.BlockProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class FlowerModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("flower", FlowerContentTypeLoader.class));
        this.facet().add(FlowerContentTypeLoader.class);
        this.bind(Flower.Builder.class).to(FlowerBuilder.class);
        this.registry().module(Flower.class, FlowerRegistryModule.class);
        this.processors().add(BlockProcessor.class);
    }

    private SingleTypeProcessorBinder<Flower, Flower.Builder, ConfigProcessor<? extends Flower.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends Flower.Builder>>() {});
    }
}
