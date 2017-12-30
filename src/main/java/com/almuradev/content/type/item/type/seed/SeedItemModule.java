/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed;

import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.seed.processor.CropSeedItemContentProcessor;
import com.almuradev.content.type.item.type.seed.processor.SoilSeedItemContentProcessor;
import com.almuradev.content.type.item.type.seed.processor.grass.GrassSeedItemContentProcessor;
import net.minecraft.block.BlockTallGrass;

public final class SeedItemModule extends ItemModule.Module {

    @Override
    @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
    protected void configure() {
        this.bind(SeedItem.Builder.class).to(SeedItemBuilder.class);
        this.processors()
                .only(CropSeedItemContentProcessor.class, ItemGenre.SEED)
                .only(SoilSeedItemContentProcessor.class, ItemGenre.SEED)
                .only(GrassSeedItemContentProcessor.class, ItemGenre.SEED);
        this.requestStaticInjection(BlockTallGrass.class);
    }
}
