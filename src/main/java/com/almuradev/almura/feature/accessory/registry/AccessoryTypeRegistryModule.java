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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccessoryTypeRegistryModule implements CatalogRegistryModule<AccessoryType> {

    private final Path configRoot;
    private final Logger logger;

    private final Map<String, Class<? extends Accessory>> parentTypeByClass = new HashMap<>();
    private Map<String, AccessoryType> accessoryTypes = new HashMap<>();

    @Inject
    public AccessoryTypeRegistryModule(@ConfigDir(sharedRoot = false) final Path configRoot, final Logger logger) {
        this.configRoot = configRoot;
        this.logger = logger;

        // TODO I dislike this but it is better than having them provide the full classpath in config :/.
        this.parentTypeByClass.put("almura:halo", HaloAccessory.class);
        this.parentTypeByClass.put("almura:wings", WingsAccessory.class);
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        final Path accessoryPath = this.configRoot.resolve(AccessoryConfig.CONFIG_NAME);

        boolean canContinue = true;
        boolean exists = true;
        if (Files.notExists(accessoryPath)) {
            try {
                Files.createDirectories(accessoryPath.getParent());
            } catch (IOException e) {
                canContinue = false;
                e.printStackTrace();
            }

            exists = false;
        }

        if (canContinue) {
            final ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setPath(accessoryPath)
                    .setRenderOptions(
                            ConfigRenderOptions.defaults()
                                    .setFormatted(true)
                                    .setComments(true)
                                    .setOriginComments(false)
                    )
                    .build();

            if (!exists) {
                try {
                    loader.save(loader.createEmptyNode());
                } catch (IOException e) {
                    canContinue = false;
                    e.printStackTrace();
                }
            }

            if (canContinue) {
                ConfigurationNode rootNode = null;
                try {
                    rootNode = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (rootNode != null) {
                    final ConfigurationNode accessoriesNode = rootNode.getNode(AccessoryConfig.ACCESSORIES);
                    if (!accessoriesNode.isVirtual()) {
                        accessoriesNode.getChildrenMap().forEach((namespace, types) -> {

                            String parentId = String.valueOf(namespace);
                            String parentDomain = parentId.split(":")[0];

                            final Class<? extends Accessory> typeClass = this.parentTypeByClass.get(parentId);
                            if (typeClass == null) {
                                this.logger.error("Unknown accessory type '{}' in configuration.", parentId);

                            } else {
                                types.getChildrenMap().forEach((subtype, properties) -> {

                                    final String id = parentId + "_" + subtype;
                                    final String name = properties.getNode(AccessoryConfig.NAME).getString(id);
                                    List<String> textures = null;
                                    try {
                                        textures = properties.getNode(AccessoryConfig.TEXTURES).getList(TypeToken.of(String.class));
                                    } catch (ObjectMappingException e) {
                                        e.printStackTrace();
                                    }

                                    if (textures == null || textures.isEmpty()) {
                                        this.logger.warn("No textures were provided for '{}'. Will render with a missing texture...", id);
                                    }

                                    final List<ResourceLocation> textureLayers = new ArrayList<>();

                                    if (textures != null) {
                                        textures.forEach((texture) -> textureLayers.add(new ResourceLocation(parentDomain, texture)));
                                    }

                                    this.accessoryTypes.put(id, new AlmuraAccessoryType(id, name, typeClass, textureLayers.toArray(new
                                            ResourceLocation[textureLayers.size()])));
                                });
                            }
                        });
                    }
                }
            }
        }
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
