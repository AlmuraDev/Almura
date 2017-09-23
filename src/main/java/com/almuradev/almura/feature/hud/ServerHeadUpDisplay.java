/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.core.server.ServerConfiguration;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.kyori.membrane.facet.Activatable;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

public class ServerHeadUpDisplay extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;
    private final MappedConfiguration<ServerConfiguration> config;

    @Inject
    private ServerHeadUpDisplay(final Game game, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final MappedConfiguration<ServerConfiguration> config) {
        this.game = game;
        this.network = network;
        this.config = config;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void clientJoin(final ClientConnectionEvent.Join event) {
        final Player player = event.getTargetEntity();

        this.network.sendTo(player, this.createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket(false);

        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            this.network.sendTo(viewer, packet);
        }

        this.network.sendTo(player, packet);
    }

    @Listener(order = Order.LAST)
    public void clientDisconnect(final ClientConnectionEvent.Disconnect event) {
        final Player player = event.getTargetEntity();

        this.network.sendTo(player, this.createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket(true);
        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            if (viewer == player) {
                continue;
            }
            this.network.sendTo(viewer, packet);
        }
    }

    @Listener(order = Order.LAST)
    public void playerMove(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.network.sendTo(player, this.createWorldNamePacket(event.getToTransform()));
        }
    }

    @Listener(order = Order.LAST)
    public void respawnPlayer(final RespawnPlayerEvent event) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.network.sendTo(event.getTargetEntity(), this.createWorldNamePacket(event.getToTransform()));
        }
    }

    private ClientboundPlayerCountPacket createPlayerCountPacket(final boolean disconnect) {
        final Server server = this.game.getServer();
        // Subtract one from the online player count when a player is disconnecting
        // due to the player not being removed from the online players list until
        // after the disconnect event has been posted.
        final int online = server.getOnlinePlayers().size() - (disconnect ? 1 : 0);
        return new ClientboundPlayerCountPacket(online, server.getMaxPlayers());
    }

    private ClientboundWorldNamePacket createWorldNamePacket(final Transform<World> transform) {
        return new ClientboundWorldNamePacket(this.getWorldName(transform));
    }

    private String getWorldName(final Transform<World> transform) {
        final String name = transform.getExtent().getName();
        return this.config.get().world.friendlyNames.getOrDefault(name, name);
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
