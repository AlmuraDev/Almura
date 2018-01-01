/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor;

import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.content.type.item.type.food.FoodItem;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface FoodItemContentProcessor extends ItemContentProcessor<FoodItem, FoodItem.Builder> {

    interface AnyTagged extends FoodItemContentProcessor, TaggedConfigProcessor<FoodItem.Builder, ConfigTag> {

    }
}
