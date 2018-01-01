/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.type.item.ContentItemType;
import com.almuradev.content.type.item.type.food.processor.foodeffect.FoodEffect;

import java.util.List;

import javax.annotation.Nullable;

public final class FoodItemBuilder extends ContentItemType.Builder.Impl<FoodItem> implements FoodItem.Builder {

    boolean alwaysEdible = false;
    List<Apply> apply;
    int durationTicks;
    FoodEffect foodEffect;

    @Override
    public FoodItem.Builder alwaysEdible(boolean alwaysEdible) {
        this.alwaysEdible = alwaysEdible;
        return this;
    }

    @Override
    public FoodItem.Builder apply(@Nullable List<Apply> apply) {
        this.apply = apply;
        return this;
    }

    @Override
    public FoodItem.Builder durationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
        return this;
    }

    @Override
    public FoodItem.Builder foodEffect(@Nullable FoodEffect foodEffect) {
        this.foodEffect = foodEffect;
        return this;
    }

    @Override
    public FoodItem build() {
        return new FoodItemImpl(this);
    }
}
