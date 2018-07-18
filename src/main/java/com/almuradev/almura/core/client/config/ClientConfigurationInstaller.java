/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client.config;

import com.almuradev.almura.shared.config.ConfigLoadEvent;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ClientConfigurationInstaller implements Witness {

    private final MappedConfiguration<ClientConfiguration> adapter;

    @Inject
    public ClientConfigurationInstaller(final MappedConfiguration<ClientConfiguration> adapter) {
        this.adapter = adapter;
    }

    @Listener
    public void onGamePreinitialization(final GamePreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.post(new ConfigLoadEvent<>(ClientConfiguration.class, this.adapter));
    }
}
