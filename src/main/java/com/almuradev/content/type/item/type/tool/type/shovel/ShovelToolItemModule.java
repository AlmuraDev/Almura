/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.shovel;

import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.tool.processor.TierProcessor;

public final class ShovelToolItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(ShovelToolItem.Builder.class).to(ShovelToolItemBuilder.class);
        this.processors()
            .only(TierProcessor.class, ItemGenre.TOOL_SHOVEL);
    }
}
