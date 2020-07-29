/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiToggleVisualsRequestPacket;
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

public final class ServerboundClaimGuiToggleVisualsRequestPacketHandler implements MessageHandler<ServerboundClaimGuiToggleVisualsRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager serverClaimManager;

    @Inject
    public ServerboundClaimGuiToggleVisualsRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager serverClaimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.serverClaimManager = serverClaimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiToggleVisualsRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
                .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();
            final Claim claim = serverClaimManager.claimLookup(player, message.x, message.y, message.z, message.worldName);
            if (claim != null) { // if GP is loaded, claim should never be null.
                final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
                final boolean isAdmin = player.hasPermission("griefdefender.admin");

                if (isOwner || isAdmin) {
                    this.serverClaimManager.toggleVisuals(player, claim, message.value);
                    this.serverClaimManager.sendUpdate(player, claim, false);
                } else {
                    this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions!"), 5);
                }
            }
        }
    }
}
