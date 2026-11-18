/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks the worlds the server is loading and shows {@link WorldLoadNoticeScreen} while it does, as 3.5's ClientWorldLoadManager.
 *
 * <p>The popup opens over whatever screen is up, which comes back when it closes, except over a container's (closing the popup would close
 * the container on the server): then it opens once the container is closed. Closing it hides it until the next world starts loading. It
 * closes itself once every world has finished.</p>
 */
public final class ClientWorldLoad {

    private static final Set<String> loading = new LinkedHashSet<>();
    private static long startedAt;
    private static boolean dismissed;
    private static @Nullable WorldLoadNoticeScreen shown;

    private ClientWorldLoad() {
    }

    public static void handle(final ClientboundWorldLoad message) {
        if (message.started()) {
            Almura.LOGGER.info("Server is loading world '{}'.", message.world());
            if (loading.isEmpty()) {
                startedAt = System.currentTimeMillis();
            }
            loading.add(message.world());
            dismissed = false;
            // Opened (or refreshed) by the tick handler
            return;
        }
        Almura.LOGGER.info("Server finished loading world '{}'.", message.world());
        loading.remove(message.world());
        if (loading.isEmpty()) {
            dismissed = false;
            final WorldLoadNoticeScreen screen = shown;
            shown = null;
            if (screen != null && InGameScreens.isShowing(screen)) {
                Minecraft.getInstance().gui.screen().onClose();
            }
        }
    }

    static List<String> worlds() {
        return new ArrayList<>(loading);
    }

    static long startedAt() {
        return startedAt;
    }

    private static void reset() {
        loading.clear();
        dismissed = false;
        shown = null;
    }

    public static final class Hooks {

        @SubscribeEvent
        public void onClientTick(final ClientTickEvent.Post event) {
            final WorldLoadNoticeScreen screen = shown;
            final Minecraft minecraft = Minecraft.getInstance();
            if (screen != null) {
                if (InGameScreens.isShowing(screen)) {
                    return;
                }
                // Closed by the player (Close or Escape) or replaced by another screen: not again until the next world
                shown = null;
                dismissed = !loading.isEmpty();
            }
            if (loading.isEmpty() || dismissed) {
                return;
            }
            final LocalPlayer player = minecraft.player;
            if (player == null || minecraft.level == null || player.isDeadOrDying()) {
                return;
            }
            // Closing the popup over a container's screen would close the container on the server: wait for it to be closed first
            if (player.containerMenu != player.inventoryMenu) {
                return;
            }
            final WorldLoadNoticeScreen notice = new WorldLoadNoticeScreen();
            shown = notice;
            InGameScreens.open(notice, minecraft.gui.screen());
        }

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            reset();
        }

        @SubscribeEvent
        public void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
            reset();
        }
    }
}
