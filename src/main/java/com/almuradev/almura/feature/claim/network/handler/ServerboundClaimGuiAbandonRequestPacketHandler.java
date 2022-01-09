/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiAbandonRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import com.griefdefender.api.claim.Claim;
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

public final class ServerboundClaimGuiAbandonRequestPacketHandler implements MessageHandler<ServerboundClaimGuiAbandonRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager serverClaimManager;

    @Inject
    public ServerboundClaimGuiAbandonRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager serverClaimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.serverClaimManager = serverClaimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiAbandonRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
                .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();
            final Claim claim = serverClaimManager.claimLookup(player, message.x, message.y, message.z, message.worldName);
            if (claim != null) {
                final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
                final boolean isAdmin = player.hasPermission(ServerClaimManager.adminPermission);

                if (isOwner || isAdmin) {
                    this.serverClaimManager.abandonClaim(claim);
                    this.notificationManager.sendPopupNotification(player, ServerClaimManager.notificationTitle, Text.of("Claim Abandoned!"), 5);
                } else {
                    this.notificationManager.sendPopupNotification(player, ServerClaimManager.notificationTitle, Text.of("Insufficient Permissions!"), 5);
                }
            }
        }
    }
}

