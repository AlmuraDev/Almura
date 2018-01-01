/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.food.processor.AlwaysEdibleFoodItemContentProcessor;
import com.almuradev.content.type.item.type.food.processor.ApplyFoodItemContentProcessor;
import com.almuradev.content.type.item.type.food.processor.DurationTicksFoodItemContentProcessor;
import com.almuradev.content.type.item.type.food.processor.PotionEffectsFoodItemContentProcessor;

public final class FoodItemModule extends ItemModule.Module {

    @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
    @Override
    protected void configure() {
        this.bind(FoodItem.Builder.class).to(FoodItemBuilder.class);
        this.processors()
                .only(AlwaysEdibleFoodItemContentProcessor.class, ItemGenre.FOOD)
                .only(ApplyFoodItemContentProcessor.class, ItemGenre.FOOD)
                .only(DurationTicksFoodItemContentProcessor.class, ItemGenre.FOOD)
                .only(PotionEffectsFoodItemContentProcessor.class, ItemGenre.FOOD);
    }
}
