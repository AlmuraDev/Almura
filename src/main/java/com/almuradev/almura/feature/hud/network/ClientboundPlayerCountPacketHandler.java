/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerCountPacketHandler implements MessageHandler<ClientboundPlayerCountPacket> {

    private final HeadUpDisplay config;

    @Inject
    private ClientboundPlayerCountPacketHandler(final HeadUpDisplay config) {
        this.config = config;
    }

    @Override
    public void handleMessage(ClientboundPlayerCountPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            this.config.onlinePlayerCount = message.online;
            this.config.maxPlayerCount = message.max;
        }
    }
}
