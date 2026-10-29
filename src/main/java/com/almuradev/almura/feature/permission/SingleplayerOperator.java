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

import java.util.Map;
import java.util.Optional;

/**
 * Gives the owner of a singleplayer world every permission: makes them an operator (Sponge's own permission service grants
 * everything to ops), and with a permissions plugin such as LuckPerms grants them * there. Vanilla doesn't op the owner, and
 * has no /op command outside dedicated servers. Never on a dedicated server, nor for players joining over LAN.
 */
public final class SingleplayerOperator implements Witness {

    // Every permission, to LuckPerms and the other permissions plugins
    private static final String ALL = "*";
    // Permissions that op or * would grant but that take things away, set to false (a permission set explicitly beats both):
    // Nucleus vanishes players with nucleus.vanish.onlogin and keeps those with its AFK exemptions from going AFK. (Almura's
    // server features stay closed in singleplayer whatever the permissions, see SingleplayerFeatures.)
    private static final String[] DENIED = {
            "nucleus.vanish.onlogin",
            "nucleus.afk.exempt.toggle",
            "nucleus.afk.exempt.kick"
    };

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

        grantPermissions(player);
    }

    /**
     * Sponge's own permission service gives an op every permission; a permissions plugin (LuckPerms) replaces it, and to it an
     * op is nobody special, so the owner is given * there. Either way those in {@link #DENIED} are set to false (both honour a
     * permission set explicitly over op or the wildcard). Through Sponge's API, so it works with any plugin; every join sets
     * them again (Sponge's own service only keeps them until the game closes).
     */
    private static void grantPermissions(final Player player) {
        final Optional<ProviderRegistration<PermissionService>> service = Sponge.getServiceManager().getRegistration(PermissionService.class);
        if (!service.isPresent()) {
            return;
        }
        final SubjectData data = player.getSubjectData();
        final Map<String, Boolean> permissions = data.getPermissions(SubjectData.GLOBAL_CONTEXT);
        final boolean permissionPlugin = !service.get().getPlugin().getId().equals("sponge");
        if (permissionPlugin && !Boolean.TRUE.equals(permissions.get(ALL))) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, ALL, Tristate.TRUE);
        }
        for (final String denied : DENIED) {
            if (!Boolean.FALSE.equals(permissions.get(denied))) {
                data.setPermission(SubjectData.GLOBAL_CONTEXT, denied, Tristate.FALSE);
            }
        }
    }
}
