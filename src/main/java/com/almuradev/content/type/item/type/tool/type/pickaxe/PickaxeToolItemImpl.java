/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.pickaxe;

import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.item.ItemPickaxe;

public final class PickaxeToolItemImpl extends ItemPickaxe implements PickaxeToolItem {
    PickaxeToolItemImpl(final PickaxeToolItemBuilder builder) {
        super(Delegate.require(builder.tier, ToolMaterial.class));
        this.tabToDisplayOn = null;
        builder.fill(this);
    }
}
