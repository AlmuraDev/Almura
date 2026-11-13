/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Shows Almura's disconnected screen in place of vanilla's, as on 1.12: when a connection fails, or a server kicks the player or goes away.
 */
public final class Disconnected {

    /**
     * Vanilla's screen opens, then Almura's takes its place on the next frame, before vanilla's is ever drawn.
     */
    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        // Exactly vanilla's: a mod's screen that extends it is left alone
        if (event.getNewScreen() == null || event.getNewScreen().getClass() != DisconnectedScreen.class) {
            return;
        }
        final DisconnectedScreen vanilla = (DisconnectedScreen) event.getNewScreen();
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.schedule(() -> {
            // Unless something else has opened since
            if (minecraft.gui.screen() == vanilla) {
                AlmuraScreens.open(new DisconnectedMenuScreen(vanilla));
            }
        });
    }
}
