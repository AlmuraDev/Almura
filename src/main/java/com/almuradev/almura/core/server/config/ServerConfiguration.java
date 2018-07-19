/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server.config;

import com.almuradev.almura.shared.config.MappedHoconConfiguration;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

import javax.inject.Singleton;

@ConfigSerializable
public final class ServerConfiguration {

    @Setting public final WorldCategory world = new WorldCategory();

    public static final class Module extends AbstractModule implements CommonBinder {

        private static final String FILE_NAME = "server.json";

        @Override
        protected void configure() {
            this.facet().add(ServerConfigurationInstaller.class);
        }

        @Provides
        @Singleton
        MappedConfiguration<ServerConfiguration> configurationAdapter(@ConfigDir(sharedRoot = false) final Path root) {
            return new MappedHoconConfiguration<ServerConfiguration>(ServerConfiguration.class, root.resolve(FILE_NAME)) {
                @Override
                protected ConfigurationLoader<? extends ConfigurationNode> createLoader() {
                    return GsonConfigurationLoader.builder()
                            .setIndent(4)
                            .setPath(this.path)
                            .build();
                }
            };
        }
    }
}
