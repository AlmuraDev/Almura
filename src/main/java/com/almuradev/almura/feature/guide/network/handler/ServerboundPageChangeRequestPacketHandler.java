/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.ServerPageManager;
import com.almuradev.almura.feature.guide.network.ClientboundPageChangeResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.feature.guide.network.PageChangeType;
import com.almuradev.almura.feature.guide.network.ServerboundPageChangeRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;

import java.time.Instant;

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
        if (side.isServer() && connection instanceof PlayerConnection && message.changeType != null && Sponge.isServerAvailable()) {

            final MinecraftServer server = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(server, message, this, connection, side)) {
                final Player player = ((PlayerConnection) connection).getPlayer();

                final boolean addPlayerGuide = message.changeType == PageChangeType.ADD && message.playerGuide;

                // Alert player that ID was missing (the server picks the ids of player guides)
                if (message.id.isEmpty() && !addPlayerGuide) {
                    this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                            "almura.guide.action.any.missing_id"));
                    return;
                }

                // Alert player that name was missing
                if (message.changeType != PageChangeType.REMOVE && message.name.trim().isEmpty()) {
                    this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                            "almura.guide.action.any.missing_name"));
                    return;
                }

                Page page = addPlayerGuide ? null : this.manager.getPage(message.id).orElse(null);

                if (addPlayerGuide) {
                    if (!player.hasPermission(ServerPageManager.PERMISSION_PLAYER_ADD)) {
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                "almura.guide.permission.add.missing"));
                        return;
                    }
                    if (this.manager.getPlayerGuideCount(player.getUniqueId()) >= this.manager.getPlayerGuideLimit(player)) {
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                "almura.guide.action.add.player_limit"));
                        return;
                    }
                    final String name = message.name.trim();
                    page = new Page(this.manager.newPlayerPageId(player.getUniqueId(), name), player.getUniqueId(), Instant.now(),
                            player.getUniqueId());
                    page.setIndex(this.manager.getNextPlayerGuideIndex(player.getUniqueId()));

                    this.manager.addPage(page);
                } else if (message.changeType == PageChangeType.ADD) {
                    // If the id being sent up is already in the manager, we've got a desync
                    if (page != null) {
                        this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager.getListingsFor(player), null));
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                "almura.guide.action.add.id_exists"));
                        return;
                    } else {
                        if (!player.hasPermission("almura.guide.add")) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.permission.add.missing"));
                            return;
                        }
                        // Server guide ids are file names, and player/ is where player guides are known
                        if (!message.id.matches("[a-z0-9_-]+")) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.action.add.invalid_id"));
                            return;
                        }
                        page = new Page(message.id, player.getUniqueId());

                        this.manager.addPage(page);
                    }
                } else if (message.changeType == PageChangeType.MODIFY || message.changeType == PageChangeType.REMOVE) {
                    // Sent up a modify or remove of a page but someone deleted it, we've got a desync
                    if (page == null) {
                        this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager.getListingsFor(player), null));
                        return;
                    }

                    if (message.changeType == PageChangeType.MODIFY) {
                        if (!this.manager.canModify(player, page)) {
                            this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id,
                                    "almura.guide.permission.modify.missing"));
                            return;
                        }
                    } else if (message.changeType == PageChangeType.REMOVE) {
                        if (!this.manager.canRemove(player, page)) {
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
                    // Player guides are ordered with their arrows (the index set when created or moved), not the details screen
                    page.setIndex(page.isPlayerGuide() ? page.getIndex() : message.index);
                    page.setName(message.name.trim());
                    page.setContent(message.content);
                    if (message.changeType == PageChangeType.ADD) {
                        this.manager.savePage(page, false);
                    } else {
                        this.manager.savePage(page, true);
                    }
                }

                final String pageId = page != null ? page.getId() : message.id;

                // Let the player know they were successful
                this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, true, pageId,
                        "almura.guide.action." + message.changeType.name().toLowerCase() + ".success"));

                // Sync the listings to the player who caused this change and put them on that page (only to fix switching on creation).
                this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager.getListingsFor(player),
                        message.changeType == PageChangeType.REMOVE ? null : pageId));

                // Sync everyone else their own listings
                this.manager.syncListingsToAll(player.getUniqueId());
            }
        }
    }
}
