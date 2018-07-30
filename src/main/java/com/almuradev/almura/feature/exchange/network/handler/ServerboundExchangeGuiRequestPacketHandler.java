/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.ServerExchangeManager;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundExchangeGuiRequestPacketHandler implements MessageHandler<ServerboundExchangeGuiRequestPacket> {

    private final ServerExchangeManager exchangeManager;

    @Inject
    public ServerboundExchangeGuiRequestPacketHandler(final ServerExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @Override
    public void handleMessage(ServerboundExchangeGuiRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

            checkNotNull(message.type);

            final Player player = ((PlayerConnection) connection).getPlayer();

            switch (message.type) {
                case MANAGE:
                    this.exchangeManager.handleExchangeManage(player);
                    break;
                case SPECIFIC:
                    this.exchangeManager.handleExchangeSpecific(player, message.id);
                    break;
                default:
                    throw new UnsupportedOperationException(message.type + " is not supported yet!");
            }

        }
    }
}
