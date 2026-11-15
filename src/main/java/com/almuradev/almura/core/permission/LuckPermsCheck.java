/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.permission;

import com.almuradev.almura.Almura;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * LuckPerms' answer for a permission by name. Only loaded with LuckPerms present; see {@link RuntimePermissions}.
 */
final class LuckPermsCheck {

    private LuckPermsCheck() {
    }

    /**
     * @return true or false as LuckPerms has it set (wildcards such as {@code almura.*} included, in the player's current contexts), or null if
     * nothing is set for it, LuckPerms isn't running, or the player isn't loaded
     */
    static @Nullable Boolean check(final UUID player, final String permission) {
        final LuckPerms luckPerms;
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (final IllegalStateException e) {
            return null;
        }
        final User user = luckPerms.getUserManager().getUser(player);
        if (user == null) {
            return null;
        }
        final QueryOptions options = luckPerms.getContextManager().getQueryOptions(user)
            .orElseGet(() -> luckPerms.getContextManager().getStaticQueryOptions());
        final Tristate result = user.getCachedData().getPermissionData(options).checkPermission(permission);
        Almura.LOGGER.debug("LuckPerms: {} has {} = {}", player, permission, result);
        return result == Tristate.UNDEFINED ? null : result.asBoolean();
    }
}
