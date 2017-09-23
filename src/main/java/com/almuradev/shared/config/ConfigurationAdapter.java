/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.config;

import com.typesafe.config.ConfigRenderOptions;
import net.kyori.lunar.exception.Exceptions;
import net.minecraftforge.common.MinecraftForge;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigurationAdapter<T extends Configuration> {

    private final Class<T> type;
    private final Path path;
    private final HoconConfigurationLoader loader;
    private final ObjectMapper<T>.BoundInstance mapper;
    private ConfigurationNode root;
    private T config;

    public ConfigurationAdapter(final Class<T> type, final Path path, final ConfigurationOptions options) {
        this.type = type;
        this.path = path;
        this.loader = HoconConfigurationLoader.builder()
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .setDefaultOptions(options)
                .setPath(this.path)
                .build();
        try {
            this.mapper = ObjectMapper.forClass(type).bindToNew();
        } catch (final ObjectMappingException e) {
            throw new RuntimeException("Failed to construct mapper for config class [" + type + "]!", e);
        }
        this.init();
        this.root = SimpleCommentedConfigurationNode.root(options);
    }

    private void init() {
        if (Files.notExists(this.path.getParent())) {
            try {
                Files.createDirectories(this.path.getParent());
            } catch (final IOException e) {
                throw Exceptions.rethrow(e);
            }
            this.save();
        }
        this.load();
    }

    public T getConfig() {
        return this.config;
    }

    public void load() {
        try {
            this.root = this.loader.load();
            this.config = this.mapper.populate(this.root);
        } catch (final IOException | ObjectMappingException e) {
            throw Exceptions.rethrow(e);
        }
        MinecraftForge.EVENT_BUS.post(new ConfigLoadEvent<>(this.type, this));
    }

    public void save() {
        try {
            this.mapper.serialize(this.root);
            this.loader.save(this.root);
        } catch (final IOException | ObjectMappingException e) {
            throw Exceptions.rethrow(e);
        }
    }
}
