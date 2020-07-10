/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFood.class)
public interface ItemFoodAccessor {
    //public-f net.minecraft.item.ItemFood field_77855_a # itemUseDuration
    @Mutable @Accessor("itemUseDuration") int accessor$getItemUseDuration();
    @Mutable @Accessor("itemUseDuration") void accessor$setItemUseDuration(int duration);

    //public-f net.minecraft.item.ItemFood field_77853_b # healAmount
    @Mutable @Accessor("healAmount") int accessor$getHealAmount();
    @Mutable @Accessor("healAmount") void accessor$setHealAmount(int amount);

    //public-f net.minecraft.item.ItemFood field_77854_c # saturationModifier
    @Mutable @Accessor("saturationModifier") float accessor$getSaturationModifier();
    @Mutable @Accessor("saturationModifier") void accessor$setSaturationModifier(float modifier);

    //public net.minecraft.item.ItemFood field_77852_bZ # alwaysEdible
    @Accessor("alwaysEdible") boolean accessor$getAlwaysEdible();
    @Accessor("alwaysEdible") void accessor$setAlwaysEdible(boolean value);
}
