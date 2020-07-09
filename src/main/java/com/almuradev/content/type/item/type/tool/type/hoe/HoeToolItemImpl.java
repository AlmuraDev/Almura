/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.hoe;

import com.almuradev.almura.asm.mixin.accessors.item.ItemAccessor;
import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.item.ItemHoe;

public final class HoeToolItemImpl extends ItemHoe implements HoeToolItem {
    HoeToolItemImpl(final HoeToolItemBuilder builder) {
        super(Delegate.require(builder.tier, ToolMaterial.class));
        ((ItemAccessor) (Object) this).accessor$setTabToDisplayOn(null);
        builder.fill(this);
    }
}
