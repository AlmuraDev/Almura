/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ClientboundTitleGuiResponsePacket;
import com.almuradev.almura.feature.title.network.ServerboundTitleGuiRequestPacket;
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

public final class ServerboundTitleGuiRequestPacketHandler implements MessageHandler<ServerboundTitleGuiRequestPacket> {

    private final ServerTitleManager manager;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ClientNotificationManager clientNotificationManager;

    @Inject
    public ServerboundTitleGuiRequestPacketHandler(final ServerTitleManager manager, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding
            .IndexedMessageChannel network, final ClientNotificationManager clientNotificationManager) {
        this.manager = manager;
        this.network = network;
        this.clientNotificationManager = clientNotificationManager;
    }

    @Override
    public void handleMessage(final ServerboundTitleGuiRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
                .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission(Almura.ID + ".title.manage")) {
                clientNotificationManager
                        .queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Insufficient Permissions to change Title!"), 2));
                return;
            }

            // TODO Remove me, test code
            this.manager.getAvailableTitlesFor(player)
                    .ifPresent(availableTitles -> this.network.sendTo(player, new ClientboundAvailableTitlesResponsePacket(availableTitles)));

            this.network.sendTo(player, new ClientboundTitleGuiResponsePacket(player.hasPermission("almura.title.admin")));
        }
    }
}
