/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.content.registry.AbstractCatalogRegistryModule;
import com.almuradev.content.registry.EagerCatalogRegistration;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

@EagerCatalogRegistration
public final class ItemGroupRegistryModule extends AbstractCatalogRegistryModule.Mapped<ItemGroup, CreativeTabs> implements AdditionalCatalogRegistryModule<ItemGroup> {
    // This is the number of vanilla groups plus an alias (see below).
    private static final int INITIAL_CAPACITY = 13 + 1;

    private ItemGroupRegistryModule() {
        super(INITIAL_CAPACITY);
    }

    @Override
    public void registerDefaults() {
        // HACK: add manual mapping for "building_blocks" - an automatic mapping for "buildingblocks" already exists
        this.provideCatalogMap().put("building_blocks", (ItemGroup) CreativeTabs.BUILDING_BLOCKS);
    }

    public static ItemGroupRegistryModule get() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        static final ItemGroupRegistryModule INSTANCE = new ItemGroupRegistryModule();
    }
}
