/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.sickle;

import com.almuradev.content.type.item.type.tool.ToolItem;

public final class SickleToolItemBuilder extends ToolItem.Builder.Impl<SickleToolItem> implements SickleToolItem.Builder {
    @Override
    public SickleToolItem build() {
        return new SickleToolItemImpl(this);
    }
}
