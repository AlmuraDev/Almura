/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ServerboundAvailableTitlesRequestPacket;
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

import java.util.Set;

import javax.inject.Inject;

public final class ServerboundAvailableTitlesRequestPacketHandler implements MessageHandler<ServerboundAvailableTitlesRequestPacket> {

    private final ServerTitleManager titleManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundAvailableTitlesRequestPacketHandler(final ServerTitleManager titleManager, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding
        .IndexedMessageChannel network) {
        this.titleManager = titleManager;
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundAvailableTitlesRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

            final Player player = ((PlayerConnection) connection).getPlayer();

            final Set<Title> availableTitles = this.titleManager.getAvailableTitlesFor(player).orElse(null);

            if (availableTitles == null) {
                // TODO Tell the player they have no available titles :'(
                return;
            } else {
                this.network.sendTo(player, new ClientboundAvailableTitlesResponsePacket(availableTitles));
            }
        }
    }
}
