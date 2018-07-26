/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.ServerExchangeManager;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
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

public final class ServerboundExchangeGuiRequestPacketHandler implements MessageHandler<ServerboundExchangeGuiRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerExchangeManager exchangeManager;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerboundExchangeGuiRequestPacketHandler(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network,
        final ServerExchangeManager exchangeManager, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.exchangeManager = exchangeManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public void handleMessage(ServerboundExchangeGuiRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission(Almura.ID + ".exchange.open")) {
                // TODO Notification
                return;
            }

            this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(message.id));
        }
    }
}
