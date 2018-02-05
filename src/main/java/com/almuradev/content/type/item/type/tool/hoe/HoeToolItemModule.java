/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.hoe;

import com.almuradev.content.type.item.ItemModule;

public final class HoeToolItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(HoeToolItem.Builder.class).to(HoeToolItemBuilder.class);
    }
}
