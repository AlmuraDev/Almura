/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.minecraft.StrataScreen;
import org.jspecify.annotations.Nullable;

/**
 * Opens Almura's in-game windows (the Features menu, the notification history) as 1.12 did: without pausing the game (pausing stopped
 * Sponge's scheduler too) or darkening the world, going back to the screen they were opened from, or to the game, when closed.
 */
public final class InGameScreens {

    private InGameScreens() {
    }

    /**
     * @param parent the screen to go back to; null for the game
     */
    public static void open(final Screen screen, final net.minecraft.client.gui.screens.@Nullable Screen parent) {
        Minecraft.getInstance().gui.setScreen(new Host(screen, parent));
    }

    /**
     * Whether the window is the screen shown.
     */
    public static boolean isShowing(final Screen screen) {
        return Minecraft.getInstance().gui.screen() instanceof final Host host && host.window == screen;
    }

    private static final class Host extends StrataScreen {

        private final Screen window;
        private final net.minecraft.client.gui.screens.@Nullable Screen parent;

        Host(final Screen screen, final net.minecraft.client.gui.screens.@Nullable Screen parent) {
            super(Component.empty(), screen, AlmuraScreens.THEME_REF);
            this.window = screen;
            this.parent = parent;
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
            // The world shows as it is
        }

        @Override
        public void onClose() {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
