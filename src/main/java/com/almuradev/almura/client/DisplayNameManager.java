/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

/**
 * Manages display name and titles for all {@link EntityPlayer}s on a client's session.
 */
@SideOnly(Side.CLIENT)
public class DisplayNameManager {

    final static Map<String, String> DISPLAY_NAMES_BY_NAME = Maps.newHashMap();
    final static Map<String, String> TITLES_BY_NAME = Maps.newHashMap();

    public static Optional<String> getDisplayName(String commandSenderName) {
        return Optional.fromNullable(DISPLAY_NAMES_BY_NAME.get(commandSenderName));
    }

    public static Optional<String> getTitle(String commandSenderName) {
        return Optional.fromNullable(TITLES_BY_NAME.get(commandSenderName));
    }

    public static Optional<String> putDisplayName(String commandSenderName, String displayName) {
        return Optional.fromNullable(DISPLAY_NAMES_BY_NAME.put(commandSenderName, displayName));
    }

    public static Optional<String> putTitle(String commandSenderName, String title) {
        return Optional.fromNullable(TITLES_BY_NAME.put(commandSenderName, title));
    }

    public static Tuple<Optional<String>, Optional<String>> getDisplayNameAndTitle(String commandSenderName) {
        return new Tuple<>(getDisplayName(commandSenderName), getTitle(commandSenderName));
    }

    public static Map<String, Tuple<Optional<String>, Optional<String>>> getAll() {
        final Map<String, Tuple<Optional<String>, Optional<String>>> toReturn = Maps.newHashMap();

        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayer player = (EntityPlayer) obj;

            toReturn.put(player.getCommandSenderName(), getDisplayNameAndTitle(player.getCommandSenderName()));
        }

        return toReturn;
    }

    public void reset() {
        DISPLAY_NAMES_BY_NAME.clear();
        TITLES_BY_NAME.clear();
    }

    public static class Tuple<T, U> {

        public final T left;
        public final U right;

        public Tuple(T left, U right) {
            this.left = left;
            this.right = right;
        }
    }
}
