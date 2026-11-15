/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.server;

/**
 * Hooks Almura up to the server's Skills plugin (2.0, on SpongeAPI 21) when it has one, as 3.5's {@code SkillsModule}. Touches nothing of
 * Sponge's or Skills' itself, so a server without them loads none of their classes.
 */
public final class ServerSkills {

    private ServerSkills() {
    }

    public static void install() {
        if (present("org.spongepowered.api.Sponge") && present("org.inspirenxe.skills.api.Skills")) {
            SpongeSkills.install();
        }
    }

    private static boolean present(final String className) {
        try {
            Class.forName(className, false, ServerSkills.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
