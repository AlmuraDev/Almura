/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.normal;

import com.almuradev.content.type.item.ContentItemType;

public final class NormalItemBuilder extends ContentItemType.Builder.Impl<NormalItem> implements NormalItem.Builder {

    @Override
    public NormalItem build() {
        return new NormalItemImpl(this);
    }
}
