/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.registry;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.AbstractCatalogRegistryModule;
import com.almuradev.shared.registry.EagerCatalogRegistration;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

@EagerCatalogRegistration
public final class ItemGroupRegistryModule extends AbstractCatalogRegistryModule.Mapped<ItemGroup, CreativeTabs> implements AdditionalCatalogRegistryModule<ItemGroup> {

    private ItemGroupRegistryModule() {
        super(13 + 1);
    }

    @Override
    public void registerDefaults() {
        // HACK: add manual mapping for "building_blocks" - an automatic mapping for "buildingblocks" already exists
        this.provideCatalogMap().put("building_blocks", (ItemGroup) CreativeTabs.BUILDING_BLOCKS);
    }

    public static ItemGroupRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {

        static final ItemGroupRegistryModule INSTANCE = new ItemGroupRegistryModule();
    }
}
