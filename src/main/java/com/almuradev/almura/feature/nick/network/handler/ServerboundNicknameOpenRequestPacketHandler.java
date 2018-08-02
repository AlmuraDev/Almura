/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.nick.network.ClientboundNicknameOpenResponsePacket;
import com.almuradev.almura.feature.nick.network.ServerboundNicknameOpenRequestPacket;
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
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

public final class ServerboundNicknameOpenRequestPacketHandler implements MessageHandler<ServerboundNicknameOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerboundNicknameOpenRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.notificationManager = notificationManager;
    }

    @Override
    public void handleMessage(ServerboundNicknameOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer threadListener = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(threadListener, message, this, connection, side)) {
                final Player player = ((PlayerConnection) connection).getPlayer();

                if (!player.hasPermission(Almura.ID + ".nickname.base")) {
                    this.notificationManager.sendPopupNotification(player, Text.of("Nickname Manager"), Text.of("Insufficient Permissions to view Nickname Manager!"), 2);
                    return;
                }

                this.network.sendTo(player, new ClientboundNicknameOpenResponsePacket(player.hasPermission(Almura.ID + ".nickname.change"), player.hasPermission(Almura.ID + ".nickname.admin")));
            }
        }
    }
}
