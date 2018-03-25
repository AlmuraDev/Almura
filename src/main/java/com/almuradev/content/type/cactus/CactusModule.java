/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.cactus.processor.BlockProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class CactusModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("cactus", CactusContentTypeLoader.class));
        this.facet().add(CactusContentTypeLoader.class);
        this.bind(Cactus.Builder.class).to(CactusBuilder.class);
        this.registry().module(Cactus.class, CactusRegistryModule.class);
        this.processors().add(BlockProcessor.class);
    }

    private SingleTypeProcessorBinder<Cactus, Cactus.Builder, ConfigProcessor<? extends Cactus.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends Cactus.Builder>>() {});
    }
}
