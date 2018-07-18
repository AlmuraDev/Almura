/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBucketMilk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemBucketMilk.class, remap = false)
public abstract class MixinItemBucketMilk extends Item {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.setMaxStackSize(64);
    }
}