/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import net.minecraft.item.ItemStack;

public interface IMixinCreativeTabs {

    void setIconItemStack(ItemStack itemStack);
}
