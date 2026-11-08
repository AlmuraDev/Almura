/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network.handler;

import com.almuradev.almura.feature.hud.network.ClientboundPongPacket;
import com.almuradev.almura.feature.hud.network.ServerboundPingPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundPingPacketHandler implements MessageHandler<ServerboundPingPacket> {

    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    private ServerboundPingPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @Override
    public void handleMessage(final ServerboundPingPacket message, final RemoteConnection connection, final Platform.Type side) {
        // Answered right here on the network thread, not queued for the next tick, so the ping measures the connection and not the
        // server's tick (vanilla's tab list ping includes both ends' game threads, so it reads hundreds of ms even on localhost).
        if (side.isServer() && connection instanceof PlayerConnection) {
            this.network.sendTo(((PlayerConnection) connection).getPlayer(), new ClientboundPongPacket(message.time));
        }
    }
}
