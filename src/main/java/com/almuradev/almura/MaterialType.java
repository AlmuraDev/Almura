/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.creativetab.CreativeTab;

public interface MaterialType extends BuildableCatalogType {

    interface Builder<MATERIAL extends MaterialType, BUILDER extends Builder<MATERIAL, BUILDER>>
            extends BuildableCatalogType.Builder<MATERIAL, BUILDER> {

        BUILDER unlocalizedName(String dictName);

        BUILDER creativeTab(CreativeTab tab);

        @Override
        default MATERIAL build(String id, String name) {
            return build(id);
        }

        MATERIAL build(String id);
    }

}
