/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.server;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A player's LuckPerms primary group, as 3.5's memberships read it. Only loaded with LuckPerms present; see {@link ServerMemberships}.
 */
final class LuckPermsGroups {

    private LuckPermsGroups() {
    }

    /**
     * @return the player's primary group, or null if LuckPerms isn't running or hasn't loaded them
     */
    static @Nullable String primaryGroup(final UUID player) {
        try {
            final User user = LuckPermsProvider.get().getUserManager().getUser(player);
            return user == null ? null : user.getPrimaryGroup();
        } catch (final IllegalStateException e) {
            return null;
        }
    }
}
