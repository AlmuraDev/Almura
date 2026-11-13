/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Puts the Server Status screen in place of vanilla's connect screen, and keeps the connection going while it shows.
 */
public final class ServerStatus {

    /**
     * Vanilla opens its connect screen and only then starts the connection, so the Server Status screen takes its place on the next frame,
     * before the connect screen is ever drawn.
     */
    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof final ConnectScreen connecting)) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.schedule(() -> {
            // Unless vanilla has already moved on, as it does when the address is unknown
            if (minecraft.gui.screen() == connecting) {
                AlmuraScreens.open(new ServerStatusScreen(connecting));
            }
        });
    }

    /**
     * Vanilla's connect screen drives its connection from its tick, which it only gets while it shows.
     */
    @SubscribeEvent
    public void onClientTick(final ClientTickEvent.Post event) {
        final ConnectScreen connecting = ServerStatusScreen.showingConnection();
        if (connecting != null) {
            connecting.tick();
        }
    }
}
