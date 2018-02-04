/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.type.item.ContentItemType;

public interface ToolItem extends ContentItemType {
    interface Builder<T extends ToolItem> extends ContentItemType.Builder<T> {
    }
}
