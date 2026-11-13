/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.options;

import com.almuradev.almura.feature.menu.AlmuraScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.inspirenxe.strata.minecraft.StrataScreen;

/**
 * Opens Almura's options, from vanilla's options ("Almura...") or from the mod list's Config button, going back there when done.
 */
public final class AlmuraOptions {

    private AlmuraOptions() {
    }

    public static void register(final ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) (mod, parent) -> screen(parent));
    }

    /**
     * Opens Almura's options over the screen given, which shows again when they close.
     */
    public static void open(final Screen parent) {
        Minecraft.getInstance().gui.setScreen(screen(parent));
    }

    private static Screen screen(final Screen parent) {
        return new Host(parent);
    }

    private static final class Host extends StrataScreen {

        private final Screen parent;

        Host(final Screen parent) {
            super(Component.translatable("almura.menu_button.options"), new AlmuraOptionsScreen(), AlmuraScreens.THEME_REF);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            // Back to the options or the mod list it was opened from
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
