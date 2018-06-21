/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network.handler;

import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeOpenResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.*;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

public final class ServerboundReviveRequestPacketHandler implements MessageHandler<ServerboundReviveRequestPacket> {

    private final Scheduler scheduler;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundReviveRequestPacketHandler(final Scheduler scheduler, final PluginContainer container, final @ChannelId(NetworkConfig
            .CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.scheduler = scheduler;
        this.container = container;
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundReviveRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer server = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(server, message, this, connection, side)) {
                final PlayerConnection playerConnection = (PlayerConnection) connection;
                final Player player = playerConnection.getPlayer();

                if (!player.hasPermission("almura.revive")) {
                    player.sendMessage(Text.of(TextColors.WHITE, "Access denied, missing permission: ", TextColors.AQUA, "almura.exchange.open",
                            TextColors.WHITE, "."));
                    return;
                }
                // Todo: teleport player back to previous loc?

            }
        }
    }
}
