/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import net.minecraft.item.ItemStack;

public interface IMixinCreativeTabs {

    void setIconItemStack(ItemStack stack);
}
