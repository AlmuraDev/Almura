/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.config;

import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.file.Path;

public class MappedHoconConfiguration<T> extends AbstractPostingMappedConfiguration<T> {

    public MappedHoconConfiguration(final Class<T> type, final Path path) {
        super(type, path);
    }

    @Override
    protected ConfigurationLoader<? extends ConfigurationNode> createLoader() {
        return HoconConfigurationLoader.builder()
                .setDefaultOptions(this.createDefaultOptions())
                .setPath(this.path)
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .build();
    }

    protected ConfigurationOptions createDefaultOptions() {
        return ConfigurationOptions.defaults();
    }
}
