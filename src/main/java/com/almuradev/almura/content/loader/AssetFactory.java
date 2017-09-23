/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.Pack;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import ninja.leaping.configurate.ConfigurationNode;

public interface AssetFactory<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final B builder);
}
