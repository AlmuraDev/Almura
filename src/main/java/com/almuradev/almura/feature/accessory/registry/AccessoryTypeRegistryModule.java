/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.registry;

import com.almuradev.almura.feature.accessory.AccessoryConfig;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.type.AlmuraAccessoryType;
import com.almuradev.almura.feature.accessory.type.Accessory;
import com.almuradev.almura.feature.accessory.type.HaloAccessory;
import com.almuradev.almura.feature.accessory.type.WingsAccessory;
import com.google.common.reflect.TypeToken;
import com.typesafe.config.ConfigRenderOptions;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccessoryTypeRegistryModule implements CatalogRegistryModule<AccessoryType> {

    private final Path configRoot;
    private final Logger logger;

    private Map<String, AccessoryType> accessoryTypes = new HashMap<>();

    @Inject
    public AccessoryTypeRegistryModule(@ConfigDir(sharedRoot = false) final Path configRoot, final Logger logger) {
        this.configRoot = configRoot;
        this.logger = logger;
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.accessoryTypes.put("almura:halo_test", new AlmuraAccessoryType("almura:halo_test", "Halo Test", HaloAccessory.class));
        this.accessoryTypes.put("almura:wings_test", new AlmuraAccessoryType("almura:wings_test", "Wings Test", WingsAccessory.class));
    }

    @Override
    public Optional<AccessoryType> getById(String id) {
        return Optional.ofNullable(this.accessoryTypes.get(id));
    }

    @Override
    public Collection<AccessoryType> getAll() {
        return Collections.unmodifiableCollection(this.accessoryTypes.values());
    }
}
