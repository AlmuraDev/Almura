/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.feature.menu.MainMenuManager;
import com.almuradev.almura.feature.menu.ServerStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Almura's client entry point. NeoForge only constructs it on the client, so the screens, renderers and other client-only code start from here
 * and the server never loads them.
 */
@Mod(value = Almura.ID, dist = Dist.CLIENT)
public final class AlmuraClient {

    public AlmuraClient(final IEventBus modBus) {
        modBus.addListener(AlmuraClient::onClientSetup);
        NeoForge.EVENT_BUS.register(new MainMenuManager());
        NeoForge.EVENT_BUS.register(new ServerStatus());
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        // Skip vanilla's first-launch accessibility screen (the narrator prompt): setup finishes before the game picks its first screens
        event.enqueueWork(() -> {
            final Options options = Minecraft.getInstance().options;
            if (options.onboardAccessibility) {
                options.onboardingAccessibilityFinished();
            }
        });
    }
}
