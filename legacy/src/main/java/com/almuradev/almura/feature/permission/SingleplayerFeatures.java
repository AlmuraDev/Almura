/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.permission;

import net.malisis.core.MalisisCore;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Almura's server features (the features menu, guide, claims, nicknames, titles, memberships, banking, exchanges and stores)
 * are for the Almura server, not singleplayer worlds: their screens don't open there. Only in the built game, so they can
 * still be worked on in a development client.
 */
public final class SingleplayerFeatures {

    private SingleplayerFeatures() {
    }

    /**
     * @return Whether the game is a singleplayer world (the server built into the client, LAN included) in the built game
     */
    public static boolean blocked() {
        return MalisisCore.isObfEnv && !((MinecraftServer) Sponge.getServer()).isDedicatedServer();
    }

    /**
     * Tells the player the feature isn't available, if it's blocked.
     *
     * @return Whether the feature is blocked (its screen must not open)
     */
    public static boolean blocked(final Player player) {
        if (!blocked()) {
            return false;
        }
        player.sendMessage(Text.of(TextColors.WHITE, "This is only available on the Almura server, not in singleplayer."));
        return true;
    }
}
