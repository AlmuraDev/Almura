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

public interface FoodItem extends ContentItemType {

    interface Builder extends ContentItemType.Builder<FoodItem> {

        Builder alwaysEdible(boolean alwaysEdible);

        Builder apply(final List<Apply> apply);

        Builder durationTicks(int durationTicks);

        Builder foodEffect(@Nullable FoodEffect foodEffect);
    }
}
