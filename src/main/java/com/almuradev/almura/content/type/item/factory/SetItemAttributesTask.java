/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import ninja.leaping.configurate.ConfigurationNode;

public class SetItemAttributesTask implements AssetFactory<BuildableItemType, BuildableItemType.Builder<?, ?>> {

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableItemType.Builder<?, ?> builder) {
    }
}
