/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network.handler;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.network.ClientboundPongPacket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPongPacketHandler implements MessageHandler<ClientboundPongPacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundPongPacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @Override
    public void handleMessage(final ClientboundPongPacket message, final RemoteConnection connection, final Platform.Type side) {
        // Timed here on the network thread, before anything waits for the next client frame
        if (side.isClient()) {
            this.hudData.ping = (int) ((System.nanoTime() - message.time) / 1_000_000L);
        }
    }
}
