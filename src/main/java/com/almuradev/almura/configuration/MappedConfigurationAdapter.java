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
    private final Path configFolder, configPath;
    private final String fileName;
    private final HoconConfigurationLoader loader;
    private final ObjectMapper<T>.BoundInstance mapper;

    private ConfigurationNode root;
    private T config;

    public MappedConfigurationAdapter(Class<T> configClass, ConfigurationOptions options, Path configFolder, String fileName) {
        this.configClass = configClass;
        this.configFolder = configFolder;
        this.fileName = fileName;
        this.configPath = this.configFolder.resolve(this.fileName);
        this.loader = HoconConfigurationLoader.builder()
                .setRenderOptions(ConfigRenderOptions.defaults().setFormatted(true).setComments(true).setOriginComments(false))
                .setDefaultOptions(options)
                .setPath(this.configPath).build();
        try {
            this.mapper = ObjectMapper.forClass(configClass).bindToNew();
        } catch (ObjectMappingException e) {
            throw new RuntimeException("Failed to construct mapper for config class [" + configClass + "]!", e);
        }
        this.root = SimpleCommentedConfigurationNode.root(options);
    }

    public Class<T> getConfigClass() {
        return this.configClass;
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

    public void loadDefaultConfig() throws IOException, ObjectMappingException {
        if (Files.notExists(this.configFolder)) {
            Files.createDirectories(this.configFolder);
        }

        this.save();
        this.load();
    }
}
