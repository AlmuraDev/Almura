/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.common;

import com.almuradev.almura.content.ContentModule;
import com.almuradev.almura.content.loader.AssetLoader;
import com.almuradev.almura.feature.hud.HeadUpDisplayModule;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.shared.asset.AssetFactoryInstaller;
import com.almuradev.shared.event.WitnessModule;
import com.almuradev.shared.inject.CommonBinder;
import com.almuradev.shared.network.NetworkModule;
import com.almuradev.shared.registry.binder.RegistryInstaller;
import net.kyori.violet.AbstractModule;

/**
 * A common module shared between the client and server.
 */
public final class CommonModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet()
                .add(RegistryInstaller.class)
                .add(AssetFactoryInstaller.class)
                .add(AssetLoader.class);
        this.registry().module(BossBarColorRegistryModule.class);
        this.install(new NetworkModule());
        this.install(new WitnessModule());
        this.install(new HeadUpDisplayModule());
        this.install(new ContentModule());
    }
}
