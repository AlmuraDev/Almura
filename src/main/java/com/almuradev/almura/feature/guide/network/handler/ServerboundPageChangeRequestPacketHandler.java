/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.feature.guide.ServerPageManager;
import com.almuradev.almura.feature.guide.network.ClientboundPageChangeResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.feature.guide.network.PageChangeType;
import com.almuradev.almura.feature.guide.network.ServerboundPageChangeRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;

import java.time.Instant;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class ServerboundPageChangeRequestPacketHandler implements MessageHandler<ServerboundPageChangeRequestPacket> {

    private final Game game;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerPageManager manager;

    @Inject
    public ServerboundPageChangeRequestPacketHandler(final Game game, final PluginContainer container, final @ChannelId(NetworkConfig.CHANNEL)
            ChannelBinding.IndexedMessageChannel network, final ServerPageManager manager) {
        this.game = game;
        this.container = container;
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPageChangeRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && message.changeType != null) {

            this.game.getScheduler().createTaskBuilder().delayTicks(0).execute(() -> {
                final Player player = ((PlayerConnection) connection).getPlayer();

                // Alert player that ID was missing
                if (message.id.isEmpty()) {
                    this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                            "almura.guide.action.any.missing_id"));
                    return;
                }

                // Alert player that name was missing
                if (message.changeType != PageChangeType.REMOVE && message.name.isEmpty()) {
                    this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                            "almura.guide.action.any.missing_name"));
                    return;
                }

                Page page = this.manager.getPage(message.id).orElse(null);

                if (message.changeType == PageChangeType.ADD) {
                    // If the id being sent up is already in the manager, we've got a desync
                    if (page != null) {
                        this.network
                                .sendTo(player, new ClientboundPageListingsPacket(this.manager.getAvailablePagesFor(player).entrySet().stream().map
                                        (entry -> new PageListEntry(entry.getKey(), entry.getValue().getName())).collect(Collectors.toList()), null));
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                "almura.guide.action.add.id_exists"));
                        return;
                    } else {
                        if (!player.hasPermission("almura.guide.add")) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.permission.add.missing"));
                            return;
                        }
                        page = new Page(message.id, player.getUniqueId());

                        this.manager.addPage(page);
                    }
                } else if (message.changeType == PageChangeType.MODIFY || message.changeType == PageChangeType.REMOVE) {
                    // Sent up a modify or remove of a page but someone deleted it, we've got a desync
                    if (page == null) {
                        this.network
                                .sendTo(player, new ClientboundPageListingsPacket(this.manager.getAvailablePagesFor(player).entrySet().stream().map
                                        (entry -> new PageListEntry(entry.getKey(), entry.getValue().getName())).collect(Collectors.toList()), null));
                        return;
                    }

                    if (message.changeType == PageChangeType.MODIFY) {
                        if (!player.hasPermission("almura.guide.modify." + message.id)) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.permission.modify.missing"));
                            return;
                        }
                    } else if (message.changeType == PageChangeType.REMOVE) {
                        if (!player.hasPermission("almura.guide.remove." + message.id)) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.permission.remove.missing"));
                            return;
                        }

                        this.manager.deletePage(message.id);
                    }
                }

                if (message.changeType != PageChangeType.REMOVE && page != null) {
                    page.setLastModifier(player.getUniqueId());
                    page.setLastModified(Instant.now());
                    page.setIndex(message.index);
                    page.setName(message.name);
                    page.setContent(message.content);
                    if (message.changeType == PageChangeType.ADD) {
                        this.manager.savePage(page, false);
                    } else {
                        this.manager.savePage(page, true);
                    }
                }

                // Let the player know they were successful
                this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, true, message.id,
                        "almura.guide.action." + message.changeType.name().toLowerCase() + ".success"));

                // Sync the listings to the player who caused this change and put them on that page (only to fix switching on creation).
                this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager
                        .getAvailablePagesFor(player).entrySet().stream().map(entry -> new PageListEntry(entry.getKey(), entry.getValue().getName()))
                        .collect(Collectors.toList()), message.id));

                // Sync the listings to everyone else
                this.game.getServer().getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).forEach((online) -> this
                        .network.sendTo(online, new ClientboundPageListingsPacket(this.manager
                                .getAvailablePagesFor(player).entrySet().stream()
                                .map(entry -> new PageListEntry(entry.getKey(), entry.getValue().getName()))
                                .collect(Collectors.toList()), null)));
            }).submit(this.container);
        }
    }
}
