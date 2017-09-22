/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.Pack;
import com.almuradev.shared.registry.AbstractBuilder;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import ninja.leaping.configurate.ConfigurationNode;

public interface AssetFactory<C extends BuildableCatalogType, B extends AbstractBuilder> {

    void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final B builder);
}
