/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.feature.guide.ServerPageManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.minecraft.client.resources.I18n;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class ServerboundGuideOpenRequestPacketHandler implements MessageHandler<ServerboundGuideOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerPageManager manager;

    @Inject
    public ServerboundGuideOpenRequestPacketHandler(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network,
            final ServerPageManager manager) {
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundGuideOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission("almura.guide.open")) {
                player.sendMessage(Text.of(I18n.format("almura.guide.permission.open.missing")));
                return;
            }

            // Open the GUI
            this.network.sendTo(player, new ClientboundGuideOpenResponsePacket(
                    player.hasPermission("almura.guide.add"),
                    player.hasPermission("almura.guide.remove"),
                    player.hasPermission("almura.guide.modify")));

            final Map<String, Page> pagesToSend = this.manager.getAvailablePagesFor(player);
            if (pagesToSend.size() > 0) {

                // Send the list of pages
                this.network.sendTo(player, new ClientboundPageListingsPacket(pagesToSend.entrySet().stream().map(entry -> new PageListEntry
                        (entry.getKey(), entry.getValue().getName())).collect(Collectors.toSet())));
            }
        }
    }
}
