/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoad;
import com.almuradev.almura.shared.WorldNames;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tells online players when the server loads a world after it is up, as 3.5 did: building a world (TerrainControl's set themselves up
 * then) holds the server up for a moment, and the world isn't ready until the area round its spawn has loaded, which Sponge does over the
 * ticks after.
 *
 * <p>The load is seen as the world is built ({@code ServerLevelMixin}, before its chunk generator is set up), so the word reaches players
 * before the pause: packets are written by the network threads while the server thread is busy. It is finished once the world is among the
 * server's and the chunk at its spawn has loaded, or after {@link #GIVE_UP_TICKS} at most. Worlds loaded while the server starts are
 * ignored, nobody is connected then.</p>
 */
public final class ServerWorldLoad {

    // A world whose spawn never loads (Sponge told not to keep it) is called done after this long, in ticks
    private static final int GIVE_UP_TICKS = 20 * 60;

    private static @Nullable MinecraftServer server;
    // The worlds loading, with how many ticks they have been waited on
    private static final Map<ResourceKey<Level>, Integer> loading = new LinkedHashMap<>();

    /**
     * A world is being built. Called on the thread building it, the server's.
     */
    public static void onBuilding(final MinecraftServer building, final ResourceKey<Level> dimension) {
        if (server != building || !building.isSameThread() || loading.containsKey(dimension)) {
            return;
        }
        loading.put(dimension, 0);
        final String name = WorldNames.of(building, dimension);
        Almura.LOGGER.info("World '{}' is loading, telling {} player(s).", name, building.getPlayerList().getPlayerCount());
        PacketDistributor.sendToAllPlayers(new ClientboundWorldLoad(true, name));
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        server = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent event) {
        server = null;
        loading.clear();
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (loading.isEmpty()) {
            return;
        }
        final MinecraftServer current = event.getServer();
        final Iterator<Map.Entry<ResourceKey<Level>, Integer>> each = loading.entrySet().iterator();
        while (each.hasNext()) {
            final Map.Entry<ResourceKey<Level>, Integer> entry = each.next();
            final int waited = entry.getValue() + 1;
            entry.setValue(waited);
            final ServerLevel level = current.getLevel(entry.getKey());
            if (!ready(level) && waited < GIVE_UP_TICKS) {
                continue;
            }
            each.remove();
            final String name = WorldNames.of(current, entry.getKey());
            Almura.LOGGER.info("World '{}' finished loading{}.", name, level == null ? " (it did not load)" : "");
            PacketDistributor.sendToAllPlayers(new ClientboundWorldLoad(false, name));
        }
    }

    private static boolean ready(final @Nullable ServerLevel level) {
        if (level == null) {
            return false;
        }
        final BlockPos spawn = level.getLevelData().getRespawnData().pos();
        return level.getChunkSource().hasChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
    }
}
