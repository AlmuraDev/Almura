/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.common;

import com.almuradev.almura.core.server.ServerConfiguration;
import com.almuradev.almura.feature.FeatureModule;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.almura.shared.command.binder.CommandInstaller;
import com.almuradev.almura.shared.event.WitnessModule;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.almura.shared.network.NetworkModule;
import com.almuradev.almura.shared.plugin.Plugin;
import com.almuradev.almura.shared.registry.binder.RegistryInstaller;
import com.almuradev.content.ContentModule;
import com.google.inject.name.Names;
import net.kyori.violet.AbstractModule;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A common module shared between the client and server.
 */
public final class CommonModule extends AbstractModule implements CommonBinder {
    private final Plugin plugin;

    public CommonModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        this.bind(Plugin.class).toInstance(this.plugin);
        this.bind(Path.class).annotatedWith(Names.named("assets")).toInstance(Paths.get("assets"));
        this.facet()
                .add(RegistryInstaller.class)
                .add(CommandInstaller.class);
        this.registry().module(BossBarColorRegistryModule.class);
        this.install(new NetworkModule());
        this.install(new WitnessModule());
        this.install(new ContentModule());
        this.install(new FeatureModule());
        this.facet()
                .add(ContentLoader.class);
        this.install(new ServerConfiguration.Module());
    }
}
