/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration;

import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MappedConfigurationAdapter<T extends AbstractConfiguration> {

    private final Class<T> configClass;
    private final Path configPath;
    private final HoconConfigurationLoader loader;
    private final ObjectMapper<T>.BoundInstance mapper;

    private ConfigurationNode root;
    private T config;

    public MappedConfigurationAdapter(Class<T> configClass, ConfigurationOptions options, Path configPath) {
        this.configClass = configClass;
        this.configPath = configPath;
        this.loader = HoconConfigurationLoader.builder()
                .setRenderOptions(ConfigRenderOptions.defaults().setFormatted(true).setComments(true).setOriginComments(false))
                .setDefaultOptions(options)
                .setPath(configPath).build();
        try {
            this.mapper = ObjectMapper.forClass(configClass).bindToNew();
        } catch (ObjectMappingException e) {
            throw new RuntimeException("Failed to construct mapper for config class [" + configClass + "]!", e);
        }
        this.root = SimpleCommentedConfigurationNode.root(options);
        if (Files.notExists(configPath)) {
            try {
                this.save();
            } catch (IOException | ObjectMappingException e) {
                throw new RuntimeException("Failed to save config for class [" + configClass + "] from [" + configPath + "]!", e);
            }
        }
    }

    public Class<T> getConfigClass() {
        return configClass;
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public T getConfig() {
        return this.config;
    }

    public void load() throws IOException, ObjectMappingException {
        this.root = this.loader.load();
        this.config = this.mapper.populate(this.root);
    }

    public void save() throws IOException, ObjectMappingException {
        this.mapper.serialize(this.root);
        this.loader.save(this.root);
    }
}
