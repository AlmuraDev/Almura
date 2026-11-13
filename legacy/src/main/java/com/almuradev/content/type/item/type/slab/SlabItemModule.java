/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab;

import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.slab.processor.DoubleProcessor;
import com.almuradev.content.type.item.type.slab.processor.SingleProcessor;

public final class SlabItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(SlabItem.Builder.class).to(SlabItemBuilder.class);
        this.processors()
                .only(DoubleProcessor.class, ItemGenre.SLAB)
                .only(SingleProcessor.class, ItemGenre.SLAB);
    }
}
