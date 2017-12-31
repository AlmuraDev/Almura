/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.itemgroup.processor.IconItemGroupContentProcessor;
import com.almuradev.content.type.itemgroup.processor.SortItemGroupContentProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class ItemGroupModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("item_group", ItemGroupContentTypeLoader.class));
        this.facet().add(ItemGroupContentTypeLoader.class);
        this.bind(ItemGroup.Builder.class).to(ItemGroupBuilder.class);
        this.registry().module(ItemGroup.class, ItemGroupRegistryModule.get());
        this.processors()
                .add(IconItemGroupContentProcessor.class)
                .add(SortItemGroupContentProcessor.class);
    }

    private SingleTypeProcessorBinder<ItemGroup, ItemGroup.Builder, ConfigProcessor<? extends ItemGroup.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends ItemGroup.Builder>>() {});
    }
}
