/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.material;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

import java.util.Optional;

public interface MaterialType extends BuildableCatalogType {

    Optional<ItemGroup> getItemGroup();

    interface Builder<MATERIAL extends MaterialType, BUILDER extends Builder<MATERIAL, BUILDER>>
            extends BuildableCatalogType.Builder<MATERIAL, BUILDER> {

        CatalogDelegate<ItemGroup> itemGroup();

        BUILDER itemGroup(CatalogDelegate<ItemGroup> itemGroupDelegate);

        BUILDER itemGroup(ItemGroup itemGroup);

        @Override
        default MATERIAL build(String id, String name) {
            return this.build(id);
        }

        MATERIAL build(String id);
    }

}
