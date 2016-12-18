/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.ResettableBuilder;

public interface BuildableCatalogType extends CatalogType {

    interface Builder<CATALOG extends CatalogType, BUILDER extends Builder<CATALOG, BUILDER>> extends ResettableBuilder<CATALOG, BUILDER> {

        CATALOG build(String id, String name);
    }
}
