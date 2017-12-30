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

    public FoodItemImpl(final int amount, final float saturation, final boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        }
}
