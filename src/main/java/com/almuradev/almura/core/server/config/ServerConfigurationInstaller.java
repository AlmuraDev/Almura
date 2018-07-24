/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server.config;

import com.almuradev.almura.shared.config.ConfigLoadEvent;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;

import javax.inject.Inject;

public final class ServerConfigurationInstaller implements Witness {

    private final MappedConfiguration<ServerConfiguration> configAdapter;

    @Inject
    public ServerConfigurationInstaller(final MappedConfiguration<ServerConfiguration> configAdapter) {
        this.configAdapter = configAdapter;
    }

    @Listener
    public void onGameStartingServer(final GameStartingServerEvent event) {
        this.configAdapter.load();
        MinecraftForge.EVENT_BUS.post(new ConfigLoadEvent<>(ServerConfiguration.class, this.configAdapter));
    }
}
