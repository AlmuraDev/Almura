/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.type;

import com.almuradev.almura.content.type.material.MaterialType;
import org.spongepowered.api.item.ItemType;

public interface BuildableItemType extends MaterialType, ItemType {

    interface Builder<ITEM extends BuildableItemType, BUILDER extends Builder<ITEM, BUILDER>> extends MaterialType.Builder<ITEM, BUILDER> {

        @Override
        ITEM build(String id);
    }
}
