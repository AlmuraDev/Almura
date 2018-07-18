/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.normal;

import com.almuradev.content.type.item.ItemModule;

public final class NormalItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(NormalItem.Builder.class).to(NormalItemBuilder.class);
    }
}
