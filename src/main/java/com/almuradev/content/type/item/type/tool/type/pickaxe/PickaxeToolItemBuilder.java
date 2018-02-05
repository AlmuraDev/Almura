/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.pickaxe;

import com.almuradev.content.type.item.type.tool.ToolItem;

public final class PickaxeToolItemBuilder extends ToolItem.Builder.Impl<PickaxeToolItem> implements PickaxeToolItem.Builder {
    @Override
    public PickaxeToolItem build() {
        return new PickaxeToolItemImpl(this);
    }
}
