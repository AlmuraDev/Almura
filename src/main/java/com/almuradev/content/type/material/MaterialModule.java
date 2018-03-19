/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.material.processor.MaterialProcessor;
import com.almuradev.core.CoreBinder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class MaterialModule extends AbstractModule implements CoreBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("material", MaterialContentTypeLoader.class));
        this.bind(Material.Builder.class).to(MaterialBuilder.class);
        this.registry().module(Material.class, MaterialRegistryModule.class);
        this.processors()
                .add(MaterialProcessor.class);
    }

    private SingleTypeProcessorBinder<Material, Material.Builder, ConfigProcessor<? extends Material.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends Material.Builder>>() {});
    }
}
