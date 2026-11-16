/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.server;

import com.almuradev.almura.feature.hud.network.ClientboundBiomeNames;
import com.khorn.terraincontrol.neoforge.api.TerrainControlBiomes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Tells each player the name of the biome they're in when the client can't know it: TerrainControl's biomes, named in its configs on the
 * server only (3.5's clients had TerrainControl installed and knew them). Each name is sent once per player, as they first enter the biome.
 * Without TerrainControl on the server this does nothing; its classes are only touched when it is there.
 */
public final class ServerBiomeNames {

    // Each player's biome is looked at this often, in ticks
    private static final int EVERY_TICKS = 10;

    private final boolean terrainControl = ModList.get().isLoaded("terraincontrol");
    private final Map<UUID, ResourceKey<Biome>> lastBiome = new HashMap<>();
    private final Map<UUID, Set<ResourceKey<Biome>>> sent = new HashMap<>();
    private int ticks;

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (!this.terrainControl || ++this.ticks < EVERY_TICKS) {
            return;
        }
        this.ticks = 0;
        final MinecraftServer server = event.getServer();
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            final Holder<Biome> biome = player.level().getBiome(player.blockPosition());
            final Optional<ResourceKey<Biome>> key = biome.unwrapKey();
            if (key.isEmpty() || key.get().equals(this.lastBiome.put(player.getUUID(), key.get()))) {
                continue;
            }
            final Set<ResourceKey<Biome>> known = this.sent.computeIfAbsent(player.getUUID(), id -> new HashSet<>());
            if (known.contains(key.get())) {
                continue;
            }
            final Optional<String> name = TerrainControlNames.of(biome);
            if (name.isPresent()) {
                known.add(key.get());
                PacketDistributor.sendToPlayer(player, new ClientboundBiomeNames(Map.of(key.get().identifier().toString(), name.get())));
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        // The client forgets the names on its next connection, so they are sent again then
        this.lastBiome.remove(event.getEntity().getUUID());
        this.sent.remove(event.getEntity().getUUID());
    }

    /**
     * The only class here that touches TerrainControl, loaded only when it is installed.
     */
    private static final class TerrainControlNames {

        static Optional<String> of(final Holder<Biome> biome) {
            return TerrainControlBiomes.displayName(biome);
        }
    }
}
