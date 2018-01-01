/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor;

import com.almuradev.content.component.apply.ApplyParser;
import com.almuradev.content.type.item.type.food.FoodItem;
import com.almuradev.content.type.item.type.food.FoodItemConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import javax.inject.Inject;

public final class ApplyFoodItemContentProcessor implements FoodItemContentProcessor.AnyTagged {

    private static final ConfigTag TAG = ConfigTag.create(FoodItemConfig.APPLY);
    private final ApplyParser parser;

    @Inject
    public ApplyFoodItemContentProcessor(final ApplyParser parser) {
        this.parser = parser;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(ConfigurationNode config, FoodItem.Builder builder) {
        builder.apply(parser.parse(config));
    }
}
