/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item;

import org.spongepowered.api.Sponge;

public interface FoodType extends BuildableItemType {

    static FoodType.Builder builder() {
        return Sponge.getRegistry().createBuilder(FoodType.Builder.class);
    }

    interface Builder extends BuildableItemType.Builder<FoodType, FoodType.Builder> {

    }
}
