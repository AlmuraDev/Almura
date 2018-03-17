/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.item.processor.DurabilityProcessor;
import com.almuradev.content.type.item.processor.ItemGroupProcessor;
import com.almuradev.content.type.item.processor.MaxStackSizeProcessor;
import com.almuradev.content.type.item.type.food.FoodItemModule;
import com.almuradev.content.type.item.type.normal.NormalItemModule;
import com.almuradev.content.type.item.type.seed.SeedItemModule;
import com.almuradev.content.type.item.type.tool.ToolItemModule;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class ItemModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("item", ItemContentTypeLoader.class));
        this.registry().module(ContentItem.Tier.class, TierRegistryModule.class);
        this.facet().add(ItemContentTypeLoader.class);
        this.install(new FoodItemModule());
        this.install(new NormalItemModule());
        this.install(new SeedItemModule());
        this.install(new ToolItemModule());
        this.install(new Module() {
            @Override
            protected void configure() {
                this.processors()
                        .all(DurabilityProcessor.class)
                        .all(ItemGroupProcessor.class)
                        .all(MaxStackSizeProcessor.class);
            }
        });
    }

    public static abstract class Module extends AbstractModule implements CommonBinder {
        protected final MultiTypeProcessorBinder<ItemGenre, ContentItem, ContentItem.Builder<ContentItem>, ItemContentProcessor<ContentItem, ContentItem.Builder<ContentItem>>> processors() {
            return new MultiTypeProcessorBinder<>(
                    this.binder(),
                    ItemGenre.values(),
                    new TypeLiteral<ItemGenre>() {},
                    new TypeLiteral<ItemContentProcessor<ContentItem, ContentItem.Builder<ContentItem>>>() {}
            );
        }
    }
}
