/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Shows Almura's in-game menu in place of vanilla's when the game is paused (Escape, or the window losing focus), as on 1.12. Pausing
 * without the menu (F3 and Escape) is left to vanilla.
 */
public final class GameMenu {

    /**
     * Vanilla's menu opens, then Almura's takes its place on the next frame, before vanilla's is ever drawn.
     */
    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        // Exactly vanilla's: a mod's screen that extends it is left alone
        if (event.getNewScreen() == null || event.getNewScreen().getClass() != PauseScreen.class
            || !((PauseScreen) event.getNewScreen()).showsPauseMenu()) {
            return;
        }
        final PauseScreen vanilla = (PauseScreen) event.getNewScreen();
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.schedule(() -> {
            // Unless something else has opened since
            if (minecraft.gui.screen() == vanilla) {
                AlmuraScreens.open(new GameMenuScreen());
            }
        });
    }
}
