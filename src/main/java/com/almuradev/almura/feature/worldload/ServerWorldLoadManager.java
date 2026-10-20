/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload;

import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoadPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.kyori.membrane.facet.Activatable;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Tells online players when the server loads a world after it is up, which freezes the server until the world (and its spawn area)
 * is ready.
 *
 * <p>Worlds loaded while the server starts are ignored, nobody is connected then. A world is loaded synchronously on the server
 * thread and the next tick only runs once the load has returned. Packets are written by the network threads, so the "started" packet reaches clients while the server thread is still
 * busy loading.</p>
 *
 * <p>The load is caught as early as possible through {@link WorldLoadHooks} (a mixin on the world's save handler, created before
 * the chunk generator is built, which can take a long time for TerrainControl worlds). Sponge's {@link LoadWorldEvent} and Forge's
 * {@link WorldEvent.Load} fire only after that and are kept as fallbacks. A world is only announced once per load.</p>
 */
public final class ServerWorldLoadManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final PluginContainer container;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    // Worlds announced as loading whose "finished" packet hasn't been sent yet
    private final Set<String> loading = new HashSet<>();
    private boolean online;

    @Inject
    private ServerWorldLoadManager(final Game game, final PluginContainer container, final Logger logger,
            @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.container = container;
        this.logger = logger;
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
    public void onServerStarted(final GameStartedServerEvent event) {
        this.online = true;
        WorldLoadHooks.setListener(worldName -> this.announce(worldName, "save handler"));
    }

    @Listener
    public void onServerStopping(final GameStoppingServerEvent event) {
        this.online = false;
        WorldLoadHooks.setListener(null);
    }

    // Fallbacks for loads that don't create a save handler; these only fire once the chunk generator has been built.

    @Listener(order = Order.FIRST)
    public void onLoadWorld(final LoadWorldEvent event) {
        this.announce(event.getTargetWorld().getName(), "Sponge");
    }

    @SubscribeEvent
    public void onForgeWorldLoad(final WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            this.announce(((World) event.getWorld()).getName(), "Forge");
        }
    }

    private void announce(final String worldName, final String source) {
        if (!this.online || !this.game.getServer().isMainThread()) {
            return;
        }

        if (!this.loading.add(worldName)) {
            return; // Already announced by the other event
        }

        final Collection<Player> players = this.game.getServer().getOnlinePlayers();
        this.logger.info("World '{}' is loading ({} event), notifying {} player(s).", worldName, source, players.size());
        this.sendToAll(new ClientboundWorldLoadPacket(true, worldName));

        // Runs on the next tick, once the server thread has finished loading the world
        Task.builder()
                .execute(() -> {
                    this.loading.remove(worldName);
                    this.logger.info("World '{}' finished loading.", worldName);
                    this.sendToAll(new ClientboundWorldLoadPacket(false, worldName));
                })
                .submit(this.container);
    }

    private void sendToAll(final ClientboundWorldLoadPacket packet) {
        for (final Player player : this.game.getServer().getOnlinePlayers()) {
            this.network.sendTo(player, packet);
        }
    }
}
