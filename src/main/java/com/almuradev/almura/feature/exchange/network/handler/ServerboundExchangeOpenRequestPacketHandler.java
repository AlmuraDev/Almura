/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeOpenResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundExchangeOpenRequestPacketHandler implements MessageHandler<ServerboundExchangeOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerboundExchangeOpenRequestPacketHandler(final @ChannelId(NetworkConfig
        .CHANNEL) ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.notificationManager = notificationManager;
    }

    @Override
    public void handleMessage(ServerboundExchangeOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission(Almura.ID + ".exchange.open")) {
                // TODO Notification
                return;
            }

            this.network.sendTo(player, new ClientboundExchangeOpenResponsePacket());
        }
    }
}
