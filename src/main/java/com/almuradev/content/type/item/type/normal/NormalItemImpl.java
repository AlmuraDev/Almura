/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.normal;

import net.minecraft.item.Item;

public final class NormalItemImpl extends Item implements NormalItem {

    NormalItemImpl(final NormalItemBuilder builder) {
        builder.fill(this);
    }
}
