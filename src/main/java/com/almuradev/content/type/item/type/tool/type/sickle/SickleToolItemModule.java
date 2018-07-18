/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.sickle;

import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.tool.processor.EffectiveOnProcessor;
import com.almuradev.content.type.item.type.tool.processor.TierProcessor;

public final class SickleToolItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(SickleToolItem.Builder.class).to(SickleToolItemBuilder.class);
        this.processors()
            .only(EffectiveOnProcessor.Required.class, ItemGenre.TOOL_SICKLE)
            .only(TierProcessor.class, ItemGenre.TOOL_SICKLE);
    }
}
