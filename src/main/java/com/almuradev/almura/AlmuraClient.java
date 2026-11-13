/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.hud.Huds;
import com.almuradev.almura.feature.hud.ServerHudData;
import com.almuradev.almura.network.client.ClientPayloads;
import com.almuradev.almura.feature.menu.ingame.FeaturesMenu;
import com.almuradev.almura.feature.menu.Disconnected;
import com.almuradev.almura.feature.menu.GameMenu;
import com.almuradev.almura.feature.menu.MainMenuManager;
import com.almuradev.almura.feature.menu.ServerStatus;
import com.almuradev.almura.feature.menu.WorldLoading;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderState;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderer;
import com.almuradev.almura.feature.menu.figure.FigureSlot;
import com.almuradev.almura.feature.menu.options.AlmuraOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Almura's client entry point. NeoForge only constructs it on the client, so the screens, renderers and other client-only code start from here
 * and the server never loads them.
 */
@Mod(value = Almura.ID, dist = Dist.CLIENT)
public final class AlmuraClient {

    public AlmuraClient(final IEventBus modBus, final ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, AlmuraClientConfig.SPEC);
        AlmuraOptions.register(container);
        modBus.addListener(AlmuraClient::onClientSetup);
        modBus.addListener(AlmuraClient::onRegisterPictureInPictureRenderers);
        NeoForge.EVENT_BUS.register(new MainMenuManager());
        NeoForge.EVENT_BUS.register(new ServerStatus());
        NeoForge.EVENT_BUS.register(new Disconnected());
        NeoForge.EVENT_BUS.register(new GameMenu());
        NeoForge.EVENT_BUS.register(new WorldLoading());
        NeoForge.EVENT_BUS.register(new FigureSlot.Drawer());
        Huds.register(modBus);
        // Almura's messages from the server
        ClientPayloads.register(modBus);
        NeoForge.EVENT_BUS.register(new ServerHudData.Resetter());
        FeaturesMenu.register(modBus);
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

    private static void onRegisterPictureInPictureRenderers(final RegisterPictureInPictureRenderersEvent event) {
        // The animated Dockter_Almura figure of the title screen
        event.register(AlmuraFigureRenderState.class, AlmuraFigureRenderer::new);
    }
}
