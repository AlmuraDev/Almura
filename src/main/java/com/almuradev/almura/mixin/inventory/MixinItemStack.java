/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.inventory;

import com.almuradev.almura.extension.item.IItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack implements IItemStack {
     
    @Shadow public abstract net.minecraft.item.Item getItem(); 

    @Overwrite
    public int getMaxStackSize() {
        if (isCache)
            return this.getItem().getItemStackLimit(p_i1876_1_);
        else
            return 1024;
    }    
}
