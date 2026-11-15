/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.economy;

import com.almuradev.almura.feature.economy.server.SpongeEconomy;

/**
 * Hooks Almura up to the server's economy when there is one to hook up to: Sponge's (SpongeNeo with an economy plugin such as
 * EconomyLite). Touches nothing of Sponge's itself, so a server without it loads none of Sponge's classes.
 */
public final class Economy {

    private Economy() {
    }

    public static void install() {
        if (spongePresent()) {
            SpongeEconomy.install();
        }
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
