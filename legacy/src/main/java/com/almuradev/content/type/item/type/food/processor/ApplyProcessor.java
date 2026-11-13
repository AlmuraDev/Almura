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

public final class ApplyProcessor implements AbstractFoodProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(FoodItemConfig.APPLY);
    private final ApplyParser parser;

    @Inject
    private ApplyProcessor(final ApplyParser parser) {
        this.parser = parser;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final FoodItem.Builder builder) {
        builder.apply(this.parser.parse(config));
    }
}
