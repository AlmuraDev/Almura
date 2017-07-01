/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.material;

import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.CatalogDelegate;
import com.almuradev.almura.registry.BuildableCatalogType;

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
