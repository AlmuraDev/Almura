/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

public final class ServerboundExchangeOpenRequestPacketHandler implements MessageHandler<ServerboundExchangeOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundExchangeOpenRequestPacketHandler(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundExchangeOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {
            final PlayerConnection playerConnection = (PlayerConnection) connection;
            final Player player = playerConnection.getPlayer();

            if (!player.hasPermission("almura.exchange.open")) {
                player.sendMessage(Text.of(TextColors.WHITE, "Access denied, missing permission: ", TextColors.AQUA, "almura.exchange.open",
                        TextColors.WHITE, "."));
                return;
            }

            this.network.sendTo(player, new ClientboundExchangeOpenResponsePacket());
        }
    }
}
