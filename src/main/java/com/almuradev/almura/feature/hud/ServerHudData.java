/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.feature.skills.client.ClientSkills;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
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
    // The player's money, from a server with an economy: its currency's symbol and the balance
    private static @Nullable String moneySymbol;
    private static double money;
    // Names of the biomes the client has no translation for (TerrainControl's), by biome id
    private static final Map<String, String> biomeNames = new HashMap<>();

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
     * @return The player's money as 3.5's HUD showed it, always with a dollar sign whatever the currency's own symbol ({@code $ 1,234.50}),
     *     or null until a server with an economy has said
     */
    public static @Nullable String money() {
        if (moneySymbol == null) {
            return null;
        }
        return "$ " + new DecimalFormat("#,##0.00").format(money);
    }

    public static void setMoney(final String symbol, final double balance) {
        moneySymbol = symbol;
        money = balance;
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

    /**
     * @return the biome's name as the server gave it, or null when it gave none (the biome's translation is then used)
     */
    public static @Nullable String biomeName(final String biome) {
        return biomeNames.get(biome);
    }

    public static void addBiomeNames(final Map<String, String> names) {
        biomeNames.putAll(names);
    }

    private static void forget() {
        biomeNames.clear();
        worldName = null;
        tps = null;
        playerWorlds.clear();
        playerWorldsKnown = false;
        moneySymbol = null;
        money = 0;
    }

    public static final class Resetter {

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            forget();
            // The last server's skills, as 3.5 forgot them on connecting
            ClientSkills.setSkills(List.of());
        }

        @SubscribeEvent
        public void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
            forget();
            ClientSkills.setSkills(List.of());
        }
    }
}
