/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.client;

import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.notification.history.NotificationHistory;
import com.almuradev.almura.feature.notification.history.NotificationRecord;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BooleanSupplier;

/**
 * The popup notifications the server sends, shown one at a time on the HUD, each for its own time with a short pause between them, as on
 * 1.12. Without Almura's HUD showing they go to chat instead.
 *
 * <p>Nothing sends them yet: on 1.12 they came from the server (its /notify command), which isn't ported. A dev run started with
 * {@code -PsampleNotifications} queues sample ones on joining a world.</p>
 */
public final class ClientNotifications {

    // Set with -PsampleNotifications (see build.gradle)
    private static final boolean SAMPLE = Boolean.getBoolean("almura.dev.sampleNotifications");
    // The pause between notifications, in ticks
    private static final int PAUSE = 10;

    private static final Queue<PopupNotification> queue = new ArrayDeque<>();
    private static @Nullable PopupNotification current;
    private static int ticksLeft, pauseLeft;
    // Whether Almura's HUD shows, to show a notification on (set by the HUD)
    private static BooleanSupplier hudShows = () -> false;

    private ClientNotifications() {
    }

    public static void setHudShows(final BooleanSupplier hudShows) {
        ClientNotifications.hudShows = hudShows;
    }

    /**
     * Queues a notification, to show after the ones before it.
     */
    public static void show(final PopupNotification notification) {
        // Kept in the history (F12, Notifications) as it arrives, as on 1.12
        NotificationHistory.record(NotificationRecord.TYPE_POPUP, notification.title(), notification.message());
        ClientNotifications.queue.offer(notification);
    }

    /**
     * @return The notification showing, if one is
     */
    public static @Nullable PopupNotification current() {
        return ClientNotifications.current;
    }

    /**
     * Shows the next notification when it's time, on the client tick.
     */
    public static final class Ticker {

        @SubscribeEvent
        public void onClientTick(final ClientTickEvent.Post event) {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            if (ClientNotifications.pauseLeft > 0) {
                ClientNotifications.pauseLeft--;
                return;
            }
            if (ClientNotifications.current == null) {
                final PopupNotification next = ClientNotifications.queue.poll();
                if (next == null) {
                    return;
                }
                if (ClientNotifications.hudShows.getAsBoolean()) {
                    ClientNotifications.current = next;
                    ClientNotifications.ticksLeft = next.secondsToLive() * 20;
                } else {
                    // No HUD to show it on: the message in chat
                    Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(
                        net.minecraft.network.chat.Component.literal(next.message()));
                }
            } else if (--ClientNotifications.ticksLeft <= 0) {
                ClientNotifications.current = null;
                ClientNotifications.pauseLeft = PAUSE;
            }
        }

        /**
         * A new connection starts with no notifications waiting; a dev run started with -PsampleNotifications queues sample ones.
         */
        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            ClientNotifications.queue.clear();
            ClientNotifications.current = null;
            if (SAMPLE) {
                ClientNotifications.show(new PopupNotification("§cServer Restart", "§fThe server restarts in 5 minutes.", 6));
                ClientNotifications.show(new PopupNotification("§6Almura", "§fWelcome to Almura Reborn!", 6));
            }
        }
    }
}
