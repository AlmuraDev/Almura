/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.mixin.impl;

import com.almuradev.content.mixin.MixinSupport;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ShapedRecipes.class)
public class MixinShapedRecipe {

    @ModifyVariable(
            method = "deserializeItem",
            index = 1,
            ordinal = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/JsonUtils;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private static boolean count(final boolean count) {
        return MixinSupport.useCountWithShapedRecipe || count;
    }
}
