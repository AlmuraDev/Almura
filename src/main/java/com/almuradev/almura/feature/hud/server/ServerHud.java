/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.server;

import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorlds;
import com.almuradev.almura.feature.hud.network.ClientboundServerTps;
import com.almuradev.almura.feature.hud.network.ClientboundWorldName;
import com.almuradev.almura.shared.WorldNames;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sends the players what the HUD shows of the server, as 1.12's server did: the world each is in (on joining and on changing worlds),
 * everyone's world for the player list, and the server's ticks per second once a second.
 */
public final class ServerHud {

    private static final long TPS_EVERY_NANOS = 1_000_000_000L;
    private long lastTps;

    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        final MinecraftServer server = player.level().getServer();
        PacketDistributor.sendToPlayer(player, new ClientboundWorldName(worldName(player)), new ClientboundServerTps(tps(server)));
        // The joining player learns everyone's world; everyone else, the joining player's
        final Map<UUID, String> worlds = new HashMap<>();
        for (final ServerPlayer online : server.getPlayerList().getPlayers()) {
            worlds.put(online.getUUID(), worldName(online));
        }
        PacketDistributor.sendToPlayer(player, new ClientboundPlayerWorlds(true, worlds));
        sendToOthers(server, player, worldName(player));
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player) {
            // No world: off the player list
            sendToOthers(player.level().getServer(), player, "");
        }
    }

    @SubscribeEvent
    public void onChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player) {
            worldChanged(player);
        }
    }

    @SubscribeEvent
    public void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player) {
            worldChanged(player);
        }
    }

    /**
     * Every second (by the clock, so a lagging server still reports on time), everyone is told the server's speed.
     */
    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        final long now = System.nanoTime();
        if (now - this.lastTps >= TPS_EVERY_NANOS) {
            this.lastTps = now;
            if (event.getServer().getPlayerCount() > 0) {
                PacketDistributor.sendToAllPlayers(new ClientboundServerTps(tps(event.getServer())));
            }
        }
    }

    private static void worldChanged(final ServerPlayer player) {
        final String name = worldName(player);
        PacketDistributor.sendToPlayer(player, new ClientboundWorldName(name));
        PacketDistributor.sendToAllPlayers(new ClientboundPlayerWorlds(false, Map.of(player.getUUID(), name)));
    }

    private static void sendToOthers(final MinecraftServer server, final ServerPlayer player, final String world) {
        final ClientboundPlayerWorlds payload = new ClientboundPlayerWorlds(false, Map.of(player.getUUID(), world));
        for (final ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online != player) {
                PacketDistributor.sendToPlayer(online, payload);
            }
        }
    }

    private static String worldName(final ServerPlayer player) {
        return WorldNames.of(player.level().getServer(), player.level().dimension());
    }

    private static double tps(final MinecraftServer server) {
        final float millis = server.getCurrentSmoothedTickTime();
        return millis <= 50f ? 20.0 : 1000.0 / millis;
    }
}
