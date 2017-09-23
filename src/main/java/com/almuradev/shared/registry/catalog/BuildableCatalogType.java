/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.registry.catalog;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.ResettableBuilder;

public interface BuildableCatalogType extends CatalogType {

    interface Builder<CATALOG extends BuildableCatalogType, BUILDER extends Builder<CATALOG, BUILDER>> extends ResettableBuilder<CATALOG, BUILDER> {

        CATALOG build(String id, String name);
    }
}
