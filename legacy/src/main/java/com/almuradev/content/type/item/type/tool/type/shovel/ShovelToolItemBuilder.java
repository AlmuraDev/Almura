/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.shovel;

import com.almuradev.content.type.item.type.tool.ToolItem;

public final class ShovelToolItemBuilder extends ToolItem.Builder.Impl<ShovelToolItem> implements ShovelToolItem.Builder {
    @Override
    public ShovelToolItem build() {
        return new ShovelToolItemImpl(this);
    }
}
