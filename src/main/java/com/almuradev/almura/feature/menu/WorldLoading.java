/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Shows Almura's world loading screen in place of vanilla's: joining a server, opening a world and going through a portal. Vanilla's keeps
 * going behind it (see {@link WorldLoadingHost}).
 */
public final class WorldLoading {

    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        // Exactly vanilla's: a mod's own (a dimension's transition screen) is left alone
        if (!(event.getNewScreen() instanceof final LevelLoadingScreen loading) || loading.getClass() != LevelLoadingScreen.class) {
            return;
        }
        if (Minecraft.getInstance().gui.screen() instanceof final WorldLoadingHost host) {
            // Already showing: carry on with vanilla's new screen behind it
            host.setLoading(loading);
            event.setCanceled(true);
        } else {
            event.setNewScreen(new WorldLoadingHost(loading));
        }
    }

    /**
     * No hotbar or other HUD while the world loads, as with vanilla's screen (vanilla only knows to hide it for its own).
     */
    @SubscribeEvent
    public void onRenderHud(final RenderGuiEvent.Pre event) {
        if (Minecraft.getInstance().gui.screen() instanceof WorldLoadingHost) {
            event.setCanceled(true);
        }
    }
}
