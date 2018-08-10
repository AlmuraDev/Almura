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
import com.almuradev.almura.feature.exchange.network.ServerboundModifyForSaleItemListStatusRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerboundModifyForSaleItemListStatusRequestPacketHandler
    implements MessageHandler<ServerboundModifyForSaleItemListStatusRequestPacket> {

    private final ServerExchangeManager exchangeManager;

    @Inject
    public ServerboundModifyForSaleItemListStatusRequestPacketHandler(final ServerExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @Override
    public void handleMessage(final ServerboundModifyForSaleItemListStatusRequestPacket message, final RemoteConnection connection,
        final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

            checkNotNull(message.type);

            final Player player = ((PlayerConnection) connection).getPlayer();

            switch (message.type) {
                case LIST:
                    this.exchangeManager.handleListForSaleItem(player, message.id, message.listItemRecNo, message.price);
                    break;
                case DE_LIST:
                    this.exchangeManager.handleDelistForSaleItem(player, message.id, message.listItemRecNo);
                    break;
                case ADJUST_PRICE:
                    this.exchangeManager.handleAdjustPriceForSaleItem(player, message.id, message.listItemRecNo, message.price);
                default:
                    throw new UnsupportedOperationException(message.type + " is not supported yet!");
            }
        }
    }
}
