/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.mixin.impl;

import com.almuradev.content.type.item.type.food.FoodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFood.class)
public abstract class MixinItemFood extends Item implements FoodItem {

}
