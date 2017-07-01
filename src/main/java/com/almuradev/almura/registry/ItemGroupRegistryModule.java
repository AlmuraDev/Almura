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
public final class ItemGroupRegistryModule implements AdditionalCatalogRegistryModule<ItemGroup>, RegistryHelper {

    @RegisterCatalog(ItemGroups.class)
    private final Map<String, ItemGroup> map = Maps.newHashMap();

    private ItemGroupRegistryModule() {
    }

    public static ItemGroupRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(ItemGroup group) {
        checkNotNull(group);
        final String id = group.getId().toLowerCase(Locale.ENGLISH);
        this.registerSetId(this.map, id, group);
    }

    @Override
    public Optional<ItemGroup> getById(String id) {
        return Optional.ofNullable(this.map.get(this.withDomain(id)));
    }

    @Override
    public Collection<ItemGroup> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    private static final class Holder {

        static final ItemGroupRegistryModule INSTANCE = new ItemGroupRegistryModule();
    }
}
