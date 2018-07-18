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
import com.almuradev.content.type.item.type.food.processor.AlwaysEdibleProcessor;
import com.almuradev.content.type.item.type.food.processor.ApplyProcessor;
import com.almuradev.content.type.item.type.food.processor.DurationTicksProcessor;
import com.almuradev.content.type.item.type.food.processor.PotionEffectsProcessor;

public final class FoodItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.bind(FoodItem.Builder.class).to(FoodItemBuilder.class);
        this.processors()
                .only(AlwaysEdibleProcessor.class, ItemGenre.FOOD)
                .only(ApplyProcessor.class, ItemGenre.FOOD)
                .only(DurationTicksProcessor.class, ItemGenre.FOOD)
                .only(PotionEffectsProcessor.class, ItemGenre.FOOD);
    }
}
