/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * What Almura's server has told this client for the HUD (the world's name, the server's speed, each player's world), forgotten on each new
 * connection. Until a server says, the HUD reads the game's own server when playing on this computer.
 */
public final class ServerHudData {

    private static @Nullable String worldName;
    private static @Nullable Double tps;
    private static final Map<UUID, String> playerWorlds = new HashMap<>();
    private static boolean playerWorldsKnown;

    private ServerHudData() {
    }

    public static @Nullable String worldName() {
        return worldName;
    }

    public static void setWorldName(final String name) {
        worldName = name;
    }

    public static @Nullable Double tps() {
        return tps;
    }

    public static void setTps(final double value) {
        tps = value;
    }

    /**
     * @return Each player's world, or null until the server has said
     */
    public static @Nullable Map<UUID, String> playerWorlds() {
        return playerWorldsKnown ? playerWorlds : null;
    }

    /**
     * @param replace whether these are everyone's (on joining) or changes; an empty world removes the player
     */
    public static void updatePlayerWorlds(final boolean replace, final Map<UUID, String> worlds) {
        if (replace) {
            playerWorlds.clear();
        }
        worlds.forEach((player, world) -> {
            if (world.isEmpty()) {
                playerWorlds.remove(player);
            } else {
                playerWorlds.put(player, world);
            }
        });
        playerWorldsKnown = true;
    }

    private static void forget() {
        worldName = null;
        tps = null;
        playerWorlds.clear();
        playerWorldsKnown = false;
    }

    public static final class Resetter {

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            forget();
        }

        @SubscribeEvent
        public void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
            forget();
        }
    }
}
