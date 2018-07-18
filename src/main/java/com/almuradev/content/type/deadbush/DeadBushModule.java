/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.deadbush;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.deadbush.processor.BlockProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class DeadBushModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("deadbush", DeadBushContentTypeLoader.class));
        this.facet().add(DeadBushContentTypeLoader.class);
        this.bind(DeadBush.Builder.class).to(DeadBushBuilder.class);
        this.registry().module(DeadBush.class, DeadBushRegistryModule.class);
        this.processors().add(BlockProcessor.class);
    }

    private SingleTypeProcessorBinder<DeadBush, DeadBush.Builder, ConfigProcessor<? extends DeadBush.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends DeadBush.Builder>>() {});
    }
}
