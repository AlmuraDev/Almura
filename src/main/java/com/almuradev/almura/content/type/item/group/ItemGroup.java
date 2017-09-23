/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.group;

import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(ItemGroups.class)
public interface ItemGroup extends BuildableCatalogType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    ItemStack getIcon();

    int getIndex();

    interface Builder extends BuildableCatalogType.Builder<ItemGroup, Builder> {

        Builder icon(CatalogDelegate<ItemType> item);

        Builder icon(ItemStack item);
    }
}
