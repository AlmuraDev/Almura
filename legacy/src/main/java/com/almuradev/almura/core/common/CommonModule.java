/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.common;

import com.almuradev.almura.core.server.config.ServerConfigurationModule;
import com.almuradev.almura.core.server.config.category.DatabaseCategory;
import com.almuradev.almura.feature.FeatureModule;
import com.almuradev.almura.patch.ContentMover;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import com.almuradev.almura.shared.capability.binder.CapabilityInstaller;
import com.almuradev.almura.shared.capability.impl.MultiSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.SingleSlotItemHandler;
import com.almuradev.almura.shared.command.binder.CommandInstaller;
import com.almuradev.almura.shared.database.DatabaseModule;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.almura.shared.network.NetworkModule;
import com.almuradev.almura.shared.plugin.Plugin;
import com.almuradev.content.ContentModule;
import com.almuradev.core.event.WitnessModule;
import com.almuradev.core.registry.binder.RegistryInstaller;
import com.google.inject.name.Names;
import net.kyori.violet.AbstractModule;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A common module shared between the client and server.
 */
public final class CommonModule extends AbstractModule implements CommonBinder {
    private final Plugin plugin;

    public CommonModule(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        this.bind(Plugin.class).toInstance(this.plugin);
        this.capability()
                .register(ISingleSlotItemHandler.class, new SingleSlotItemHandler.Storage(), SingleSlotItemHandler::new)
                .register(IMultiSlotItemHandler.class, new MultiSlotItemHandler.Storage(), MultiSlotItemHandler::new);
        this.bind(Path.class).annotatedWith(Names.named("assets")).toInstance(Paths.get("assets"));
        this.facet()
                .add(RegistryInstaller.class)
                .add(CommandInstaller.class)
                .add(CapabilityInstaller.class);
        this.registry().module(BossBarColorRegistryModule.class);
        this.install(new NetworkModule());
        this.install(new WitnessModule());
        this.install(new ContentModule());
        this.install(new FeatureModule());
        this.install(new ServerConfigurationModule());
        this.install(new DatabaseModule());
        this.facet().add(ContentMover.class);
        this.facet().add(ContentLoader.class);
    }
}
