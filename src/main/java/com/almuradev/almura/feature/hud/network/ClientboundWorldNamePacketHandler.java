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

public final class ClientboundWorldNamePacketHandler implements MessageHandler<ClientboundWorldNamePacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundWorldNamePacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @Override
    public void handleMessage(ClientboundWorldNamePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            this.hudData.worldName = message.name;
        }
    }
}
