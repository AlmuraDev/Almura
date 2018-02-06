/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.sickle;

import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import java.util.Collections;
import java.util.Set;

public final class SickleToolItemImpl extends ItemTool implements SickleToolItem {
    SickleToolItemImpl(final SickleToolItemBuilder builder) {
        super(Delegate.require(builder.tier, ToolMaterial.class), builder.effectiveOn());
        this.tabToDisplayOn = null;
        builder.fill(this);
    }

    // Dumb stuff added by Forge
    @Override
    public Set<String> getToolClasses(final ItemStack stack) {
        return Collections.singleton(SickleToolItem.TOOL_CLASS);
    }
}
