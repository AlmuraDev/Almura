/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.inventory;

import com.almuradev.almura.extension.item.IItemStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack implements IItemStack {
     
    @Shadow public abstract net.minecraft.item.Item getItem();

    private ItemStack this$ = (ItemStack) (Object) this;
    private boolean isCache = false;

    @Overwrite
    public int getMaxStackSize() {
        if (!this.isCache())
            return this.getItem().getItemStackLimit(this$);
        else
            return 1024;
    }

    @Override
    public boolean isCache() {
        return this.isCache;
    }

    @Override
    public void markAsCacheStack(boolean value) {
        this.isCache = value;
    }
}
