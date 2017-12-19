/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server;

import com.almuradev.almura.core.server.config.WorldCategory;
import com.almuradev.almura.shared.config.AbstractPostingMappedConfiguration;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

import javax.inject.Singleton;

@ConfigSerializable
public final class ServerConfiguration {

    @Setting public final WorldCategory world = new WorldCategory();

    public static final class Module extends AbstractModule {

        private static final String FILE_NAME = "server.json";

        @Override
        protected void configure() {
        }

        @Provides
        @Singleton
        MappedConfiguration<ServerConfiguration> configurationAdapter(@ConfigDir(sharedRoot = false) final Path root) {
            return new AbstractPostingMappedConfiguration<ServerConfiguration>(ServerConfiguration.class, root.resolve(FILE_NAME)) {
                @Override
                protected ConfigurationLoader<? extends ConfigurationNode> createLoader() {
                    return JSONConfigurationLoader.builder()
                            .setIndent(4)
                            .setPath(this.path)
                            .build();
                }
            };
        }
    }
}
