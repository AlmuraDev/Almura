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
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

import javax.inject.Singleton;

public final class ServerConfigurationModule extends AbstractModule implements CommonBinder {

    private static final String FILE_NAME = "server.conf";

    @Override
    protected void configure() {
        this.facet().add(ServerConfigurationInstaller.class);
    }

    @Provides
    @Singleton
    MappedConfiguration<ServerConfiguration> configurationAdapter(@ConfigDir(sharedRoot = false) final Path root) {
        return new MappedHoconConfiguration<ServerConfiguration>(ServerConfiguration.class, root.resolve(FILE_NAME)) {
            @Override
            protected ConfigurationNode createRoot() {
                return SimpleCommentedConfigurationNode.root(this.loader.getDefaultOptions());
            }
        };
    }
}
