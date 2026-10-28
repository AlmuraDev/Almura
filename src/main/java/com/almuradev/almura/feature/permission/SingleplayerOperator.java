/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.permission;

import com.almuradev.core.event.Witness;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Makes the owner of a singleplayer world an operator, so they have every permission without a permissions plugin (Sponge's own
 * permission service grants everything to ops). Vanilla doesn't op the owner, and has no /op command outside dedicated servers.
 */
public final class SingleplayerOperator implements Witness {

    // First, so the join listeners that check permissions (the guide at login) already see the op
    @Listener(order = Order.FIRST)
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();
        if (server.isDedicatedServer()) {
            return;
        }

        final GameProfile profile = ((EntityPlayerMP) player).getGameProfile();
        // Only the player whose world it is, not players joining it over LAN
        if (!profile.getName().equals(server.getServerOwner())) {
            return;
        }

        final PlayerList players = server.getPlayerList();
        if (players.getOppedPlayers().getEntry(profile) == null) {
            players.addOp(profile);
        }
    }
}
