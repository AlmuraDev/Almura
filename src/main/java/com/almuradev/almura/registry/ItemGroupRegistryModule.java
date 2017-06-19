/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.item.group.ItemGroups;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// TODO Special registration for buildingBlocks to instance to Building_Blocks.
public final class ItemGroupRegistryModule implements AdditionalCatalogRegistryModule<ItemGroup> {

    @RegisterCatalog(ItemGroups.class)
    private final Map<String, ItemGroup> mappings = Maps.newHashMap();

    private ItemGroupRegistryModule() {
    }

    public static ItemGroupRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(ItemGroup extraCatalog) {
        checkNotNull(extraCatalog);
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        this.mappings.put(id, extraCatalog);
    }

    @Override
    public Optional<ItemGroup> getById(String id) {
        if (!checkNotNull(id).contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.mappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<ItemGroup> getAll() {
        return Collections.unmodifiableCollection(this.mappings.values());
    }

    private static final class Holder {

        static final ItemGroupRegistryModule INSTANCE = new ItemGroupRegistryModule();
    }
}
