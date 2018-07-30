/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
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

public final class ServerboundClaimGuiRequestPacketHandler implements MessageHandler<ServerboundClaimGuiRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager claimManager;

    @Inject
    public ServerboundClaimGuiRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager claimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.claimManager = claimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission(Almura.ID + ".claim.manage")) {
                this.notificationManager
                    .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions to manage this claim!"), 2);
                return;
            }

            this.claimManager.openClientGUI(player);
        }
    }
}
