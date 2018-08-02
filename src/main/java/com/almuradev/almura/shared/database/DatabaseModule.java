/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import com.almuradev.almura.core.server.config.ServerConfiguration;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;

import javax.inject.Singleton;

public final class DatabaseModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(DatabaseManager.class);
        this.facet().add(DatabaseInstaller.class);
    }

    @Provides
    @Singleton
    DatabaseManager configurationAdapter(final PluginContainer container, final Scheduler scheduler, final ServiceManager manager, final
    MappedConfiguration<ServerConfiguration> configAdapter) {
        return new DatabaseManager(container, scheduler, manager, configAdapter.get().database);
    }
}
