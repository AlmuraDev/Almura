/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import net.minecraft.item.ItemFood;

public final class FoodItemImpl extends ItemFood implements FoodItem {

    public FoodItemImpl(int amount, float saturation, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        }
}
