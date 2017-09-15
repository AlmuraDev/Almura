/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundWorldNamePacketHandler implements MessageHandler<ClientboundWorldNamePacket> {

    private final HeadUpDisplay config;

    @Inject
    private ClientboundWorldNamePacketHandler(final HeadUpDisplay config) {
        this.config = config;
    }

    @Override
    public void handleMessage(ClientboundWorldNamePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            this.config.worldName = message.name;
        }
    }
}
