/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.axe;

import com.almuradev.content.type.item.type.tool.ToolItem;

public final class AxeToolItemBuilder extends ToolItem.Builder.Impl<AxeToolItem> implements AxeToolItem.Builder {
    @Override
    public AxeToolItem build() {
        return new AxeToolItemImpl(this);
    }
}
