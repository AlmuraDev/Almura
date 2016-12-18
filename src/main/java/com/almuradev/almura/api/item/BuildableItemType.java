/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.item;

import com.almuradev.almura.api.BuildableCatalogType;
import com.almuradev.almura.api.creativetab.CreativeTab;
import org.spongepowered.api.item.ItemType;

public interface BuildableItemType extends BuildableCatalogType, ItemType {

    interface Builder<ITEM extends BuildableItemType, BUILDER extends Builder<ITEM, BUILDER>> extends BuildableCatalogType.Builder<ITEM, BUILDER> {

        Builder<ITEM, BUILDER> creativeTab(CreativeTab tab);

        @Override
        ITEM build(String id, String name);
    }
}
