/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.type.item.ContentItem;
import com.almuradev.content.type.item.type.food.processor.foodeffect.FoodEffect;

import java.util.List;

import javax.annotation.Nullable;

public interface FoodItem extends ContentItem {
    interface Builder extends ContentItem.Builder<FoodItem> {
        void alwaysEdible(boolean alwaysEdible);

        void apply(final List<Apply> apply);

        void durationTicks(int durationTicks);

        void foodEffect(@Nullable FoodEffect foodEffect);
    }
}
