/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.nick.network.NickMessages.ClientboundOpen;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

/**
 * Nicknames on the server, as 3.5's {@code ServerNickManager}: kept by the server's Nucleus (4.0, on SpongeAPI 21) and sent to every player,
 * so a player's nickname shows on their name tag. Touches nothing of Sponge's or Nucleus' itself, so a server without them loads none of
 * their classes; there the Nicknames window says they need Nucleus.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.nickname.base}: open the Nicknames window (the Features menu's Nicknames);</li>
 *     <li>{@code almura.nickname.change}: set or remove one's nickname (Nucleus' own {@code nucleus.nick.base} is needed too, as 3.5);</li>
 *     <li>{@code almura.nickname.admin}: the window's administrator view (as 3.5, it changes nothing yet).</li>
 * </ul>
 */
public final class ServerNicks {

    static final String TITLE = "Nickname Manager";
    private static final boolean PRESENT = present("org.spongepowered.api.Sponge")
        && present("io.github.nucleuspowered.nucleus.api.module.nickname.NucleusNicknameService");

    public static final PermissionNode<Boolean> BASE = node("nickname.base");
    public static final PermissionNode<Boolean> CHANGE = node("nickname.change");
    public static final PermissionNode<Boolean> ADMIN = node("nickname.admin");

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    /**
     * Registers the nicknames' permissions, and hooks into Nucleus when the server has it.
     */
    public static void install() {
        NeoForge.EVENT_BUS.register(new ServerNicks());
        if (PRESENT) {
            SpongeNicks.install();
        }
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(BASE, CHANGE, ADMIN);
    }

    /**
     * Opens the Nicknames window, as 3.5's: for players with {@code almura.nickname.base}, on a server with Nucleus.
     */
    public static void open(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, BASE)) {
            ServerNotifications.sendPopup(player, TITLE, "Insufficient Permissions to view Nickname Manager!", 2);
            return;
        }
        if (!PRESENT || !SpongeNicks.available()) {
            ServerNotifications.sendPopup(player, TITLE, "Nicknames need Nucleus, which this server doesn't have.", 4);
            return;
        }
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(PermissionAPI.getPermission(player, CHANGE),
            PermissionAPI.getPermission(player, ADMIN)));
    }

    /**
     * The player sets their nickname, or (empty) removes it.
     */
    public static void change(final ServerPlayer player, final String nickname) {
        if (!PRESENT || !SpongeNicks.available()) {
            return;
        }
        if (!PermissionAPI.getPermission(player, CHANGE)) {
            ServerNotifications.sendPopup(player, "Nickname Error!", "Insufficient Permissions!", 5);
            return;
        }
        SpongeNicks.change(player, nickname);
    }

    private static boolean present(final String className) {
        try {
            Class.forName(className, false, ServerNicks.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
