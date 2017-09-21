/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.shared.event.Witness;
import com.almuradev.shared.network.NetworkConfig;
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

    @Inject
    private ServerHeadUpDisplay(final Game game, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.network = network;
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

        this.network.sendTo(player, createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket();

        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            this.network.sendTo(viewer, packet);
        }

        this.network.sendTo(player, packet);
    }

    @Listener(order = Order.LAST)
    public void clientDisconnect(final ClientConnectionEvent.Disconnect event) {
        final Player player = event.getTargetEntity();

        this.network.sendTo(player, createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket();
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
            this.network.sendTo(player, createWorldNamePacket(event.getToTransform()));
        }
    }

    @Listener(order = Order.LAST)
    public void respawnPlayer(final RespawnPlayerEvent event) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.network.sendTo(event.getTargetEntity(), createWorldNamePacket(event.getToTransform()));
        }
    }

    private ClientboundPlayerCountPacket createPlayerCountPacket() {
        final Server server = this.game.getServer();
        return new ClientboundPlayerCountPacket(server.getOnlinePlayers().size(), server.getMaxPlayers());
    }

    private static ClientboundWorldNamePacket createWorldNamePacket(final Transform<World> transform) {
        return new ClientboundWorldNamePacket(getWorldName(transform));
    }

    private static String getWorldName(final Transform<World> transform) {
        final String name = transform.getExtent().getName();
        switch (name) {
            case "DIM-1":
                return"The Nether";
            case "DIM1":
                return"The End";
            case "world":
                return"Dakara";
            case "cemaria":
                return"Cemaria";
            case "asgard":
                return"Asgard";
            case "atlantis":
                return"Atlantis";
            case "othala":
                return"Othala";
            case "keystone":
                return"Keystone";
        }
        return name;
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
