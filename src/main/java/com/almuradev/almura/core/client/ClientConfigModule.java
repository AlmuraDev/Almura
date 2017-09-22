/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.Constants;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.shared.config.ConfigurationAdapter;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import ninja.leaping.configurate.ConfigurationOptions;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

import javax.inject.Singleton;

final class ClientConfigModule extends AbstractModule {

    private static final String FILE_NAME = "client.conf";

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    ConfigurationAdapter<ClientConfiguration> configurationAdapter(@ConfigDir(sharedRoot = false) final Path root) {
        final ConfigurationOptions options = ConfigurationOptions.defaults()
                .setHeader(Constants.Config.HEADER);
        return new ConfigurationAdapter<>(ClientConfiguration.class, root.resolve(FILE_NAME), options);
    }
}
