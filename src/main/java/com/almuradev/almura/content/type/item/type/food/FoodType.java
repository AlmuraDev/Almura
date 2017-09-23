/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.type.food;

import com.almuradev.almura.content.type.item.type.BuildableItemType;
import org.spongepowered.api.Sponge;

public interface FoodType extends BuildableItemType {

    static FoodType.Builder builder() {
        return Sponge.getRegistry().createBuilder(FoodType.Builder.class);
    }

    interface Builder extends BuildableItemType.Builder<FoodType, FoodType.Builder> {

    }
}
