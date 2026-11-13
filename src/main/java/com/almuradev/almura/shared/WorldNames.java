/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

/**
 * The names Almura shows for worlds: the save's name for the overworld, the dimension's otherwise ("The Nether").
 */
public final class WorldNames {

    private WorldNames() {
    }

    public static String of(final MinecraftServer server, final ResourceKey<Level> dimension) {
        return dimension == Level.OVERWORLD ? server.getWorldData().getLevelName() : words(dimension.identifier().getPath());
    }

    // "the_nether" as "The Nether"
    private static String words(final String path) {
        final StringBuilder name = new StringBuilder();
        for (final String word : path.split("_")) {
            if (!word.isEmpty()) {
                name.append(name.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return name.toString();
    }
}
