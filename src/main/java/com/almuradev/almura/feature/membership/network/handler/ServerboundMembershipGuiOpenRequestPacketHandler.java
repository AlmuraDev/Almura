/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.network.handler;

import com.almuradev.almura.feature.membership.MembershipHandler;
import com.almuradev.almura.feature.membership.network.ServerboundMembershipGuiOpenRequestPacket;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;

import javax.inject.Inject;

public final class ServerboundMembershipGuiOpenRequestPacketHandler implements MessageHandler<ServerboundMembershipGuiOpenRequestPacket> {

    @Inject private static MembershipHandler membershipHandler;
    private final Scheduler scheduler;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundMembershipGuiOpenRequestPacketHandler(final Scheduler scheduler, final PluginContainer container, final @ChannelId(NetworkConfig
            .CHANNEL) ChannelBinding.IndexedMessageChannel network, MembershipHandler membershipHandler) {
        this.scheduler = scheduler;
        this.container = container;
        this.network = network;
        this.membershipHandler = membershipHandler;
    }

    @Override
    public void handleMessage(ServerboundMembershipGuiOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer server = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(server, message, this, connection, side)) {
                final PlayerConnection playerConnection = (PlayerConnection) connection;
                final Player player = playerConnection.getPlayer();

                membershipHandler.requestClientGui(player);
            }
        }
    }
}
