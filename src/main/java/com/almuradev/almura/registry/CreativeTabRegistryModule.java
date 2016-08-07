/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.api.CreativeTab;
import com.almuradev.almura.api.CreativeTabs;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// TODO Special registration for buildingBlocks to map to Building_Blocks.
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
        this.creativeTabMappings.put(checkNotNull(extraCatalog.getId()).toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Optional<CreativeTab> getById(String id) {
        return Optional.ofNullable(this.creativeTabMappings.get(checkNotNull(id)));
    }

    @Override
    public Collection<CreativeTab> getAll() {
        return Collections.unmodifiableCollection(this.creativeTabMappings.values());
    }

    @Override
    @DelayedRegistration(RegistrationPhase.INIT)
    public void registerDefaults() {
        for (net.minecraft.creativetab.CreativeTabs minecraftTab : net.minecraft.creativetab.CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (this.isInvalidTab(minecraftTab)) {
                continue;
            }
            this.registerAdditionalCatalog((CreativeTab) minecraftTab);
        }
    }

    private boolean isInvalidTab(net.minecraft.creativetab.CreativeTabs minecraftTab) {
        return minecraftTab.equals(net.minecraft.creativetab.CreativeTabs.INVENTORY) ||
                minecraftTab.equals(net.minecraft.creativetab.CreativeTabs.SEARCH);
    }

    private static final class Holder {

        static final CreativeTabRegistryModule INSTANCE = new CreativeTabRegistryModule();
    }
}
