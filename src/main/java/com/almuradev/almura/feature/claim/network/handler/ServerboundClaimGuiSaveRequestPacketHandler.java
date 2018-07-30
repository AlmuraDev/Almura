/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
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

public final class ServerboundClaimGuiSaveRequestPacketHandler implements MessageHandler<ServerboundClaimGuiSaveRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager claimManager;

    @Inject
    public ServerboundClaimGuiSaveRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager claimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.claimManager = claimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiSaveRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();
            this.claimManager.saveClaimChanges(player, message.claimName, message.claimGreeting, message.claimFarewell, message.x, message.y,
                message.z, message.worldName);
        }
    }
}
