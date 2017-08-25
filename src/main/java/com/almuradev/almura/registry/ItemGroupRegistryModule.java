/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.content.item.group.ItemGroup;

// TODO Special registration for buildingBlocks to instance to Building_Blocks.
public final class ItemGroupRegistryModule extends AbstractCatalogRegistryModule<ItemGroup> implements AbstractCatalogRegistryModule.Additional<ItemGroup> {

    private ItemGroupRegistryModule() {
        super(16);
    }

    public static ItemGroupRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {

        static final ItemGroupRegistryModule INSTANCE = new ItemGroupRegistryModule();
    }
}
