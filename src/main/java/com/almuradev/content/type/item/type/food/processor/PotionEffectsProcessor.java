/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor;

import com.almuradev.content.type.item.type.food.FoodItem;
import com.almuradev.content.type.item.type.food.FoodItemConfig;
import com.almuradev.content.type.item.type.food.processor.foodeffect.FoodEffect;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class PotionEffectsProcessor implements AbstractFoodProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(FoodItemConfig.POTION_EFFECTS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final FoodItem.Builder builder) {
        FoodEffect.PARSER.deserialize(config).ifPresent(builder::foodEffect);
    }
}
