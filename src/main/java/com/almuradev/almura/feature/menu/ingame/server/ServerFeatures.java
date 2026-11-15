/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.ingame.network.FeaturesMessages.ClientboundOpen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

/**
 * The Almura Features menu on the server, as 3.5's: who may open it and who sees its administrator buttons (Manage Exchanges, Manage Stores).
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.feature.base}: open the menu (F12);</li>
 *     <li>{@code almura.feature.admin}: its administrator buttons.</li>
 * </ul>
 */
public final class ServerFeatures {

    public static final PermissionNode<Boolean> BASE = node("feature.base");
    public static final PermissionNode<Boolean> ADMIN = node("feature.admin");

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(BASE, ADMIN);
    }

    /**
     * The player pressed F12: the menu opens if they may, as 3.5's said otherwise.
     */
    public static void open(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, BASE)) {
            player.sendSystemMessage(Component.literal("Access denied, missing permission: ")
                .append(Component.literal("almura.feature.base").withStyle(ChatFormatting.AQUA)).append(Component.literal(".")));
            return;
        }
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(PermissionAPI.getPermission(player, ADMIN)));
    }
}
