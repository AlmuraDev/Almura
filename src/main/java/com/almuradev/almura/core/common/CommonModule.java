/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.common;

import com.almuradev.almura.core.server.ServerConfiguration;
import com.almuradev.almura.feature.animal.AnimalModule;
import com.almuradev.almura.feature.hud.HeadUpDisplayModule;
import com.almuradev.almura.feature.title.TitleModule;
import com.almuradev.almura.feature.nick.NickModule;
import com.almuradev.almura.feature.notification.NotificationModule;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.almura.shared.command.binder.CommandInstaller;
import com.almuradev.almura.shared.event.WitnessModule;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.almura.shared.network.NetworkModule;
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

    @Override
    protected void configure() {
        this.bind(Path.class).annotatedWith(Names.named("assets")).toInstance(Paths.get("assets"));
        this.facet()
                .add(RegistryInstaller.class)
                .add(CommandInstaller.class);
        this.registry().module(BossBarColorRegistryModule.class);
        this.install(new NetworkModule());
        this.install(new WitnessModule());
        this.install(new ContentModule());
        this.install(new HeadUpDisplayModule());
        this.install(new NickModule());
        this.install(new TitleModule());
        this.install(new NotificationModule());
        this.install(new AnimalModule());
        this.facet()
                .add(ContentLoader.class);
        this.install(new ServerConfiguration.Module());
    }
}
