/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.creativetab.CreativeTab;
import com.almuradev.almura.creativetab.CreativeTabs;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// TODO Special registration for buildingBlocks to instance to Building_Blocks.
public final class CreativeTabRegistryModule implements AdditionalCatalogRegistryModule<CreativeTab> {

    @RegisterCatalog(CreativeTabs.class)
    private final Map<String, CreativeTab> creativeTabMappings = Maps.newHashMap();

    private CreativeTabRegistryModule() {
    }

    public static CreativeTabRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(CreativeTab extraCatalog) {
        checkNotNull(extraCatalog);
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        this.creativeTabMappings.put(id, extraCatalog);
    }

    @Override
    public Optional<CreativeTab> getById(String id) {
        if (!checkNotNull(id).contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.creativeTabMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CreativeTab> getAll() {
        return Collections.unmodifiableCollection(this.creativeTabMappings.values());
    }

    private static final class Holder {

        static final CreativeTabRegistryModule INSTANCE = new CreativeTabRegistryModule();
    }
}
