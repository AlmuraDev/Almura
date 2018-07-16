/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.mixin.impl;

import com.almuradev.content.type.item.mixin.EffectiveOn;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ItemTool.class)
public class MixinToolItem {
    @Redirect(
        method = "Lnet/minecraft/item/ItemTool;getDestroySpeed(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z",
            remap = false
        )
    )
    private boolean effectiveOn(final Set<Block> set, final Object block) {
        if(block instanceof EffectiveOn) {
            return ((EffectiveOn) block).effectiveTool((Item) (Object) this);
        }
        return set.contains(block);
    }
}
