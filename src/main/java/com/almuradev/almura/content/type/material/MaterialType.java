/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.material;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.AbstractBuilder;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

import java.util.Optional;

public interface MaterialType extends BuildableCatalogType {

    Optional<ItemGroup> getItemGroup();

    interface Builder<T extends MaterialType, B extends Builder<T, B>> extends AbstractBuilder<T, B> {

        CatalogDelegate<ItemGroup> itemGroup();

        B itemGroup(CatalogDelegate<ItemGroup> itemGroupDelegate);

        B itemGroup(ItemGroup itemGroup);

        default T build(String id, String name) {
            return this.build(id);
        }

        T build(String id);
    }
}
