/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.context;

import net.minecraft.item.ItemStack;

public interface ItemApplyContext extends ApplyContext {
    ItemStack item();
}
