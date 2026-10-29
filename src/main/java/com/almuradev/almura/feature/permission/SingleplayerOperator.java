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
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.Optional;

/**
 * Gives the owner of a singleplayer world every permission: makes them an operator (Sponge's own permission service grants
 * everything to ops), and with a permissions plugin such as LuckPerms grants them * there. Vanilla doesn't op the owner, and
 * has no /op command outside dedicated servers. Never on a dedicated server, nor for players joining over LAN.
 */
public final class SingleplayerOperator implements Witness {

    // Every permission, to LuckPerms and the other permissions plugins
    private static final String ALL = "*";

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

        grantAllInPermissionPlugin(player);
    }

    /**
     * A permissions plugin (LuckPerms) replaces Sponge's own service, and to it an op is nobody special: give the owner every
     * permission there instead, through Sponge's API so it works with any plugin. The plugin saves it, like a permission set
     * by command.
     */
    private static void grantAllInPermissionPlugin(final Player player) {
        final Optional<ProviderRegistration<PermissionService>> service = Sponge.getServiceManager().getRegistration(PermissionService.class);
        if (!service.isPresent() || service.get().getPlugin().getId().equals("sponge")) {
            return; // Sponge's own service: ops already have every permission
        }
        final SubjectData data = player.getSubjectData();
        if (!Boolean.TRUE.equals(data.getPermissions(SubjectData.GLOBAL_CONTEXT).get(ALL))) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, ALL, Tristate.TRUE);
        }
    }
}
