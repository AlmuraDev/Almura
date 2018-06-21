/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.feature.nick.network.ClientboundNicknameOpenResponsePacket;
import com.almuradev.almura.feature.nick.network.ServerboundNicknameOpenRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.*;

import javax.inject.Inject;

public final class ServerboundNicknameOpenRequestPacketHandler implements MessageHandler<ServerboundNicknameOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager manager;

    @Inject
    public ServerboundNicknameOpenRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager manager) {
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundNicknameOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer threadListener = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(threadListener, message, this, connection, side)) {
                final Player player = ((PlayerConnection) connection).getPlayer();

                // Check permission here
                // I added the notification manager above in-case you want to say you were denied opening via the GUI

                this.network.sendTo(player, new ClientboundNicknameOpenResponsePacket());
            }
        }
    }
}
