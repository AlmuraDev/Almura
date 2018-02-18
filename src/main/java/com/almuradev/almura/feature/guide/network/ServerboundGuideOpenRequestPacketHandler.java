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
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
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
                player.sendMessage(Text.of(TextColors.WHITE, "Access denied, missing permission: ", TextColors.AQUA, "almura.guide.open", TextColors.WHITE, "."));
                return;
            }

            // Open the GUI
            this.network.sendTo(player, new ClientboundGuideOpenResponsePacket(
                    2, // Requested from Player.
                    player.hasPermission("almura.guide.add"),
                    player.hasPermission("almura.guide.remove"),
                    player.hasPermission("almura.guide.modify")));

            final Map<String, Page> pagesToSend = this.manager.getAvailablePagesFor(player);
            if (pagesToSend.size() > 0) {

                final List<PageListEntry> playerListings = pagesToSend.entrySet().stream().map(entry -> new PageListEntry
                        (entry.getKey(), entry.getValue().getName())).collect(Collectors.toList());
                final PageListEntry switchToPageEntry = playerListings.stream().findFirst().orElse(null);

                // Send the list of pages
                this.network.sendTo(player, new ClientboundPageListingsPacket(playerListings, switchToPageEntry == null ? null : switchToPageEntry.getId()));
            }
        }
    }
}
