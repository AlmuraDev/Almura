/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.economy;

import com.almuradev.almura.feature.economy.server.SpongeEconomy;
import org.jspecify.annotations.Nullable;

/**
 * Hooks Almura up to the server's economy when there is one to hook up to: Sponge's (SpongeNeo with an economy plugin such as
 * EconomyLite). Touches nothing of Sponge's itself, so a server without it loads none of Sponge's classes.
 */
public final class Economy {

    private Economy() {
    }

    private static final boolean SPONGE = spongePresent();

    public static void install() {
        if (SPONGE) {
            SpongeEconomy.install();
        }
    }

    /**
     * @return whether the server has the Sponge API (SpongeNeo), whose economy Almura's features use
     */
    public static boolean available() {
        return SPONGE;
    }

    /**
     * @return the server's economy accounts, or null without an economy (no Sponge, or no economy plugin)
     */
    public static @Nullable EconomyAccounts accounts() {
        return SPONGE ? SpongeEconomy.accounts() : null;
    }

    private static boolean spongePresent() {
        try {
            Class.forName("org.spongepowered.api.Sponge", false, Economy.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
