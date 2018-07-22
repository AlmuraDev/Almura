/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.network.ClientboundExchangeOpenResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.malisis.core.MalisisCore;
import net.minecraft.client.Minecraft;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

public final class ServerboundExchangeOpenRequestPacketHandler implements MessageHandler<ServerboundExchangeOpenRequestPacket> {

    private final Scheduler scheduler;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundExchangeOpenRequestPacketHandler(final Scheduler scheduler, final PluginContainer container, final @ChannelId(NetworkConfig
            .CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.scheduler = scheduler;
        this.container = container;
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundExchangeOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {

            final MinecraftServer server = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(server, message, this, connection, side)) {
                final PlayerConnection playerConnection = (PlayerConnection) connection;
                final Player player = playerConnection.getPlayer();

                if (!player.hasPermission("almura.exchange.open") || (Minecraft.getMinecraft().isSingleplayer() && MalisisCore.isObfEnv)) {
                    player.sendMessage(Text.of(TextColors.WHITE, "Access denied, missing permission: ", TextColors.AQUA, "almura.exchange.open",
                            TextColors.WHITE, "."));
                    return;
                }

                this.network.sendTo(player, new ClientboundExchangeOpenResponsePacket());
            }
        }
    }
}
