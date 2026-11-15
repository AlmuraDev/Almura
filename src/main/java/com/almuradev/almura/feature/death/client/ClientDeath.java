/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.client;

import com.almuradev.almura.feature.death.network.DeathMessages.ClientboundDied;
import com.almuradev.almura.feature.death.network.DeathMessages.ServerboundRevive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The death screen on the client, as 3.5's {@code ClientboundPlayerDiedPacketHandler}: the server's word comes just before vanilla's death
 * screen would open, and Almura's opens in its place. Hardcore worlds, and worlds that respawn at once, keep vanilla's way.
 */
public final class ClientDeath {

    // How long the server's word waits for the player to die (it comes just before), in ticks
    private static final int WAIT = 200;
    private static final String[] REMARKS = {
        "I can't believe you died again...",
        "Death is only the beginning...",
        "The PLEBIAN... has died...",
        "Oohhh, I'm sure that's gonna be fine...",
        "Ouch.  I'll bet that leaves a scar.",
        "Seriously...",
        "Why does this keep happening?",
        "What is going on exactly?",
        "Repeating death only bring more death...",
        "Try, try, try try try Again?!?",
        "Sigh... this is getting aggravating.",
        "Failure is the fog from which we all glimpse triumph",
        "Get up and walk it off... geez.",
        "Shhh. Only dreams now....",
        "You have died...again"
    };

    private static @Nullable ClientboundDied died;
    private static String remark = "";
    private static int waited;

    private ClientDeath() {
    }

    public static void onDied(final ClientboundDied message) {
        died = message;
        remark = remark();
        waited = 0;
        // Vanilla's screen already up (it rarely comes first): Almura's takes its place
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() != null && minecraft.gui.screen().getClass() == DeathScreen.class && almurasWay(minecraft)) {
            show(minecraft);
        }
    }

    static void requestRevive() {
        ClientPacketDistributor.sendToServer(new ServerboundRevive());
    }

    private static void show(final Minecraft minecraft) {
        if (died != null) {
            minecraft.gui.setScreen(new PlayerDiedScreen(died, remark).host());
        }
    }

    private static boolean showing(final Minecraft minecraft) {
        return minecraft.gui.screen() instanceof PlayerDiedScreen.Host;
    }

    /**
     * Not in hardcore (vanilla's offers spectating instead), nor when the world respawns players at once.
     */
    private static boolean almurasWay(final Minecraft minecraft) {
        final LocalPlayer player = minecraft.player;
        return player != null && minecraft.level != null && player.shouldShowDeathScreen() && !minecraft.level.getLevelData().isHardcore();
    }

    /**
     * One of 3.5's remarks, picked once a death.
     */
    private static String remark() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getGameProfile().name().equalsIgnoreCase("chimwolfeye")) {
            return "You have become one with the Sisko, rejoice!";
        }
        return REMARKS[ThreadLocalRandom.current().nextInt(REMARKS.length)];
    }

    public static final class Hooks {

        /**
         * Vanilla's death screen opens, then Almura's takes its place on the next frame, before vanilla's is drawn (as the disconnected
         * screen's).
         */
        @SubscribeEvent
        public void onScreenOpening(final ScreenEvent.Opening event) {
            // Exactly vanilla's: a mod's screen that extends it is left alone
            if (died == null || event.getNewScreen() == null || event.getNewScreen().getClass() != DeathScreen.class) {
                return;
            }
            final Minecraft minecraft = Minecraft.getInstance();
            if (!almurasWay(minecraft)) {
                return;
            }
            final net.minecraft.client.gui.screens.Screen vanilla = event.getNewScreen();
            minecraft.schedule(() -> {
                // Unless something else has opened since
                if (minecraft.gui.screen() == vanilla) {
                    show(minecraft);
                }
            });
        }

        /**
         * Dead with nothing on screen (joined while dead, which vanilla shows no screen for): Almura's opens. The server's word is dropped if
         * the player doesn't die soon after it.
         */
        @SubscribeEvent
        public void onClientTick(final ClientTickEvent.Post event) {
            if (died == null) {
                return;
            }
            final Minecraft minecraft = Minecraft.getInstance();
            final LocalPlayer player = minecraft.player;
            if (player == null) {
                return;
            }
            if (!player.isDeadOrDying()) {
                if (++waited > WAIT && !showing(minecraft)) {
                    died = null;
                }
                return;
            }
            if (minecraft.gui.screen() == null && almurasWay(minecraft)) {
                show(minecraft);
            }
        }

        /**
         * Respawned (or revived): the screen closes, as vanilla closes its own.
         */
        @SubscribeEvent
        public void onRespawn(final ClientPlayerNetworkEvent.Clone event) {
            died = null;
            final Minecraft minecraft = Minecraft.getInstance();
            if (showing(minecraft)) {
                minecraft.gui.setScreen(null);
            }
        }

        @SubscribeEvent
        public void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
            died = null;
        }
    }
}
