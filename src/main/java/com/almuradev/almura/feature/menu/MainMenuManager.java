/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import com.almuradev.almura.feature.menu.singleplayer.WorldListScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.function.Supplier;

/**
 * Shows Almura's screens in place of vanilla's menus. Strata screens open through Strata rather than as a vanilla screen object, so each
 * replacement opens on the next frame, before the vanilla screen it replaces is ever drawn.
 */
public final class MainMenuManager {

    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        final Screen next = event.getNewScreen();
        // Exactly vanilla's: a mod's screen that extends it is left alone
        if (next != null && next.getClass() == TitleScreen.class) {
            replace(next, TitleMenuScreen::new);
        }
        // Vanilla's world list, from wherever it's opened (such as backing out of a world that failed to load), as on 1.12
        if (next != null && next.getClass() == SelectWorldScreen.class) {
            replace(next, WorldListScreen::new);
        }
    }

    private static void replace(final Screen vanilla, final Supplier<org.inspirenxe.strata.api.screen.Screen> almura) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.schedule(() -> {
            // Unless something else has opened since
            if (minecraft.gui.screen() == vanilla) {
                AlmuraScreens.open(almura.get());
            }
        });
    }
}
