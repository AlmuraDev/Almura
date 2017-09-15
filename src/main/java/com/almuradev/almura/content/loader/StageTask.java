/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.shared.registry.catalog.BuildableCatalogType;

public interface StageTask<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    void execute(AssetContext<C, B> context);
}
