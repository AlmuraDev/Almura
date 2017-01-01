/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.creativetab.CreativeTab;

import java.util.Optional;

public interface MaterialType extends BuildableCatalogType {

    Optional<CreativeTab> getCreativeTab();

    interface Builder<MATERIAL extends MaterialType, BUILDER extends Builder<MATERIAL, BUILDER>>
            extends BuildableCatalogType.Builder<MATERIAL, BUILDER> {

        BUILDER creativeTab(CreativeTab creativeTab);

        Optional<CreativeTab> creativeTab();

        @Override
        default MATERIAL build(String id, String name) {
            return build(id);
        }

        MATERIAL build(String id);
    }

}
