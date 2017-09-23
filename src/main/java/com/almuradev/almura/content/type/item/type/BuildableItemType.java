/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.type;

import com.almuradev.almura.content.type.material.MaterialType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

public interface BuildableItemType extends MaterialType, ItemType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<ITEM extends BuildableItemType, BUILDER extends Builder<ITEM, BUILDER>> extends MaterialType.Builder<ITEM, BUILDER> {

        @Override
        default ITEM build(String id, String name) {
            return build(id);
        }

        @Override
        ITEM build(String id);
    }
}
