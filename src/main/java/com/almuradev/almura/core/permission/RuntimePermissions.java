/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.permission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

/**
 * Permissions only known while the server runs, such as a store's or an exchange's, which NeoForge's permission system can't take: its nodes
 * are registered only while the server starts. As 3.5 asked Sponge's permission service for any permission by name, these are asked of
 * LuckPerms by name. Without LuckPerms only operators have them.
 * <p>
 * Touches nothing of LuckPerms' itself, so a server without it loads none of its classes.
 */
public final class RuntimePermissions {

    private static final boolean LUCKPERMS = luckPermsPresent();

    private RuntimePermissions() {
    }

    /**
     * Whether the player has the permission ({@code namespace.path}, as {@code almura.store.seedbin}): LuckPerms' answer, or with nothing
     * set for it (or without LuckPerms), whether they are an operator.
     */
    public static boolean has(final ServerPlayer player, final String permission) {
        if (LUCKPERMS) {
            final Boolean answer = LuckPermsCheck.check(player.getUUID(), permission);
            if (answer != null) {
                return answer;
            }
        }
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static boolean luckPermsPresent() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider", false, RuntimePermissions.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
