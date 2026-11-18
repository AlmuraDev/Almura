/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.permission.server;

import com.almuradev.almura.Almura;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Gives the owner of a singleplayer world every permission, as 3.5's: makes them an operator (vanilla only does with cheats allowed, and
 * has no /op outside dedicated servers), and with LuckPerms grants them {@code *} there, since to LuckPerms an operator is nobody special.
 * Never on a dedicated server, nor for players joining over LAN. Almura's server features stay closed in singleplayer whatever the
 * permissions (the Features menu and the Guide say so).
 */
public final class SingleplayerOperator {

    // Every permission, to LuckPerms
    private static final String ALL = "*";
    // Permissions that * would grant but that take things away, set to false (a permission set explicitly beats the wildcard): Nucleus
    // vanishes players with nucleus.vanish.onlogin and keeps those with its AFK exemptions from going AFK
    private static final String[] DENIED = {
        "nucleus.vanish.onlogin",
        "nucleus.afk.exempt.toggle",
        "nucleus.afk.exempt.kick"
    };

    // First, so the login listeners that check permissions (the Guide at login) already see it
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        final MinecraftServer server = player.level().getServer();
        final NameAndId profile = new NameAndId(player.getGameProfile());
        // Only the player whose world it is, not players joining it over LAN
        if (server.isDedicatedServer() || !server.isSingleplayerOwner(profile)) {
            return;
        }
        final PlayerList players = server.getPlayerList();
        if (!players.isOp(profile)) {
            players.op(profile);
            Almura.LOGGER.info("Made {}, this world's owner, an operator", profile.name());
        }
        if (ModList.get().isLoaded("luckperms")) {
            LuckPermsGrant.grant(player);
        }
    }

    /**
     * The only class here that touches LuckPerms, loaded only when it is installed.
     */
    private static final class LuckPermsGrant {

        static void grant(final ServerPlayer player) {
            final LuckPerms luckPerms;
            try {
                luckPerms = LuckPermsProvider.get();
            } catch (final IllegalStateException e) {
                return;
            }
            final User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return;
            }
            boolean changed = set(user, ALL, true);
            for (final String denied : DENIED) {
                changed |= set(user, denied, false);
            }
            if (changed) {
                luckPerms.getUserManager().saveUser(user);
                Almura.LOGGER.info("Gave {}, this world's owner, every permission in LuckPerms", player.getGameProfile().name());
            }
        }

        // Sets the permission to the value, replacing it set the other way; whether anything changed
        private static boolean set(final User user, final String permission, final boolean value) {
            final PermissionNode wanted = PermissionNode.builder(permission).value(value).build();
            if (user.getNodes().contains(wanted)) {
                return false;
            }
            user.data().remove(PermissionNode.builder(permission).value(!value).build());
            user.data().add(wanted);
            return true;
        }
    }
}
