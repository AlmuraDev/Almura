/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.item;

import com.almuradev.almura.MaterialType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

public interface BuildableItemType extends MaterialType, ItemType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<ITEM extends BuildableItemType, BUILDER extends Builder<ITEM, BUILDER>> extends MaterialType.Builder<ITEM, BUILDER> {

        @Override
        ITEM build(String id, String name);
    }
}
