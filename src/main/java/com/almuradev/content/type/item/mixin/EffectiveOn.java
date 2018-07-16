/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.mixin;

import com.almuradev.content.component.delegate.DelegateSet;
import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;

import javax.annotation.Nullable;

public interface EffectiveOn {
    boolean effectiveTool(final Item item);

    void effectiveTools(final @Nullable DelegateSet<ItemType, Item> effectiveOn);
}
