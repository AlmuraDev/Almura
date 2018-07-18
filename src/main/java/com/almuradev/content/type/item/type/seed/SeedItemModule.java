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
import com.almuradev.content.type.item.type.seed.processor.CropProcessor;
import com.almuradev.content.type.item.type.seed.processor.SoilProcessor;
import com.almuradev.content.type.item.type.seed.processor.grass.GrassProcessor;
import net.minecraft.block.BlockTallGrass;

public final class SeedItemModule extends ItemModule.Module {
    @Override
    @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
    protected void configure() {
        this.bind(SeedItem.Builder.class).to(SeedItemBuilder.class);
        this.processors()
                .only(CropProcessor.class, ItemGenre.SEED)
                .only(SoilProcessor.class, ItemGenre.SEED)
                .only(GrassProcessor.class, ItemGenre.SEED);
        this.requestStaticInjection(BlockTallGrass.class);
    }
}
