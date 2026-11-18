/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.permission.RuntimePermissions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Makes WorldEdit ask LuckPerms for its permissions ({@code worldedit.selection.pos} and the rest), as it asked Sponge's permission service on
 * 3.5. WorldEdit for NeoForge only knows vanilla's: operators, or creative players with its {@code use-in-creative}; its LuckPerms support is
 * for Fabric alone. LuckPerms' answer is used when it has one; with nothing set for a permission, WorldEdit's own check decides as before.
 *
 * <p>WorldEdit's permissions provider is swapped through its public {@code CoreMcPermissionsProvider.setCurrent}, by reflection, so Almura
 * builds without WorldEdit and a server without WorldEdit or LuckPerms is left alone.</p>
 */
public final class WorldEditPermissions {

    private static final String PROVIDER = "com.sk89q.worldedit.coremc.CoreMcPermissionsProvider";

    public static boolean wanted() {
        return ModList.get().isLoaded("worldedit") && ModList.get().isLoaded("luckperms");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerStarted(final ServerStartedEvent event) {
        try {
            final Class<?> type = Class.forName(PROVIDER, true, WorldEditPermissions.class.getClassLoader());
            final Object original = type.getMethod("current").invoke(null);
            final Method hasPermission = type.getMethod("hasPermission", ServerPlayer.class, String.class);
            final Object bridge = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        default -> "Almura's LuckPerms permissions for WorldEdit";
                    };
                }
                if (method.equals(hasPermission)) {
                    final Boolean answer = RuntimePermissions.luckPerms((ServerPlayer) args[0], (String) args[1]);
                    if (answer != null) {
                        return answer;
                    }
                }
                // registerPermission, and permissions LuckPerms has nothing set for: WorldEdit's own
                try {
                    return method.invoke(original, args);
                } catch (final InvocationTargetException e) {
                    throw e.getCause();
                }
            });
            type.getMethod("setCurrent", type).invoke(null, bridge);
            Almura.LOGGER.info("Hooked into WorldEdit: its permissions come from LuckPerms");
        } catch (final ReflectiveOperationException | RuntimeException | LinkageError e) {
            Almura.LOGGER.warn("Couldn't hand WorldEdit's permissions to LuckPerms; WorldEdit keeps checking operators only", e);
        }
    }
}
