/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.file.Path;

public class MappedHoconConfiguration<T> extends com.almuradev.toolbox.config.map.AbstractMappedConfiguration<T> {

    protected MappedHoconConfiguration(final Class<T> type, final Path path) {
        super(type, path);
    }

    @Override
    protected ConfigurationLoader<? extends ConfigurationNode> createLoader() {
        return HoconConfigurationLoader.builder()
                .setPath(this.path)
                .setDefaultOptions(ConfigurationOptions.defaults())
                .build();
    }

    protected ConfigurationOptions createDefaultOptions() {
        return ConfigurationOptions.defaults();
    }
}
