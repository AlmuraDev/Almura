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

    @Inject
    public ServerboundNicknameOpenRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundNicknameOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer threadListener = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(threadListener, message, this, connection, side)) {
                final Player player = ((PlayerConnection) connection).getPlayer();

                this.network.sendTo(player, new ClientboundNicknameOpenResponsePacket());
            }
        }
    }
}
