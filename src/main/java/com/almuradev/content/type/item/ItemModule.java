/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.item.processor.ItemGroupItemContentProcessor;
import com.almuradev.content.type.item.type.normal.NormalItem;
import com.almuradev.content.type.item.type.normal.NormalItemBuilder;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.content.type.item.type.seed.SeedItemBuilder;
import com.almuradev.content.type.item.type.seed.processor.CropSeedItemContentProcessor;
import com.almuradev.content.type.item.type.seed.processor.SoilSeedItemContentProcessor;
import com.almuradev.content.type.item.type.seed.processor.grass.GrassSeedItemContentProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;
import net.minecraft.block.BlockTallGrass;

public final class ItemModule extends AbstractModule implements CommonBinder {

    @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
    @Override
    protected void configure() {
        this.bind(NormalItem.Builder.class).to(NormalItemBuilder.class);
        this.bind(SeedItem.Builder.class).to(SeedItemBuilder.class);
        this.facet().add(ItemContentTypeLoader.class);
        this.processors()
                .all(ItemGroupItemContentProcessor.class)
                .only(CropSeedItemContentProcessor.class, ItemGenre.SEED)
                .only(SoilSeedItemContentProcessor.class, ItemGenre.SEED)
                .only(GrassSeedItemContentProcessor.class, ItemGenre.SEED);
        this.requestStaticInjection(BlockTallGrass.class);

    }

    private MultiTypeProcessorBinder<ItemGenre, ContentItemType, ContentItemType.Builder<ContentItemType>, ItemContentProcessor<ContentItemType, ContentItemType.Builder<ContentItemType>>> processors() {
        return new MultiTypeProcessorBinder<>(
                this.binder(),
                ItemGenre.values(),
                new TypeLiteral<ItemGenre>() {},
                new TypeLiteral<ItemContentProcessor<ContentItemType, ContentItemType.Builder<ContentItemType>>>() {}
        );
    }
}
