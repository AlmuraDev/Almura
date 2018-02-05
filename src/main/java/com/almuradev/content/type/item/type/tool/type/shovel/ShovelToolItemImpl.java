/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.shovel;

import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.item.ItemSpade;

public final class ShovelToolItemImpl extends ItemSpade implements ShovelToolItem {
    ShovelToolItemImpl(final ShovelToolItemBuilder builder) {
        super(Delegate.require(builder.tier, ToolMaterial.class));
        builder.fill(this);
    }
}
