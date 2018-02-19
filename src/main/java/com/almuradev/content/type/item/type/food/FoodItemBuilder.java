/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.type.item.AbstractItemBuilder;
import com.almuradev.content.type.item.type.food.processor.foodeffect.FoodEffect;

import java.util.List;

import javax.annotation.Nullable;

public final class FoodItemBuilder extends AbstractItemBuilder<FoodItem> implements FoodItem.Builder {
    boolean alwaysEdible = false;
    List<Apply> apply;
    int durationTicks;
    FoodEffect foodEffect;

    @Override
    public void alwaysEdible(final boolean alwaysEdible) {
        this.alwaysEdible = alwaysEdible;
    }

    @Override
    public void apply(@Nullable final List<Apply> apply) {
        this.apply = apply;
    }

    @Override
    public void durationTicks(final int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public void foodEffect(@Nullable final FoodEffect foodEffect) {
        this.foodEffect = foodEffect;
    }

    @Override
    public FoodItem build() {
        return new FoodItemImpl(this);
    }
}
