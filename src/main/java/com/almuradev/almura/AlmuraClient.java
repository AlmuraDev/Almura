/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.content.client.ContentTooltips;
import com.almuradev.almura.content.client.StorageBarrelRenderer;
import com.almuradev.almura.content.client.StorageBarrelScreen;
import com.almuradev.almura.content.registry.ContentContainers;
import com.almuradev.almura.content.registry.FlickerLights;
import com.almuradev.almura.core.client.AlmuraFontPack;
import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.harvestsack.HarvestingSack;
import com.almuradev.almura.feature.hud.Huds;
import com.almuradev.almura.feature.hud.ServerHudData;
import com.almuradev.almura.feature.lighting.client.ClientFlickerLights;
import com.almuradev.almura.feature.lighting.client.FlickerLightRenderer;
import com.almuradev.almura.feature.menu.Disconnected;
import com.almuradev.almura.feature.menu.GameMenu;
import com.almuradev.almura.feature.menu.MainMenuManager;
import com.almuradev.almura.feature.menu.ServerStatus;
import com.almuradev.almura.feature.menu.WorldLoading;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderState;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderer;
import com.almuradev.almura.feature.menu.figure.FigureSlot;
import com.almuradev.almura.feature.menu.ingame.FeaturesMenu;
import com.almuradev.almura.feature.menu.options.AlmuraOptions;
import com.almuradev.almura.network.client.ClientPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.sdl.SDLVideo;

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
        // Almura's lettering, above any resource pack's
        AlmuraFontPack.register(modBus);
        // The storage barrel's label
        modBus.addListener((final EntityRenderersEvent.RegisterRenderers event) -> {
            if (ContentContainers.blockEntityType() != null) {
                event.registerBlockEntityRenderer(ContentContainers.blockEntityType(),
                    StorageBarrelRenderer::new);
            }
            // A flickering light's fast blink
            if (FlickerLights.blockEntityType() != null) {
                event.registerBlockEntityRenderer(FlickerLights.blockEntityType(),
                    FlickerLightRenderer::new);
            }
        });
        // The content's containers show a chest's screen
        modBus.addListener((final RegisterMenuScreensEvent event) -> {
            if (ContentContainers.menuType() != null) {
                event.register(ContentContainers.menuType(), ContainerScreen::new);
                event.register(ContentContainers.storageBarrelMenuType(), StorageBarrelScreen::new);
                // The harvesting sack shows a single chest's
                if (HarvestingSack.menuType() != null) {
                    event.register(HarvestingSack.menuType(), ContainerScreen::new);
                }
            }
        });
        NeoForge.EVENT_BUS.register(new MainMenuManager());
        NeoForge.EVENT_BUS.register(new ServerStatus());
        NeoForge.EVENT_BUS.register(new Disconnected());
        NeoForge.EVENT_BUS.register(new GameMenu());
        NeoForge.EVENT_BUS.register(new WorldLoading());
        NeoForge.EVENT_BUS.register(new FigureSlot.Drawer());
        Huds.register(modBus);
        // The content's item tooltips
        NeoForge.EVENT_BUS.register(new ContentTooltips());
        // The content's flickering lights flicker on this client
        ClientFlickerLights.install();
        // Almura's messages from the server
        ClientPayloads.register(modBus);
        NeoForge.EVENT_BUS.register(new ServerHudData.Resetter());
        // Dev runs pass -Dalmura.dev.maximize=true (see build.gradle) to open the window maximized
        if (Boolean.getBoolean("almura.dev.maximize")) {
            NeoForge.EVENT_BUS.addListener(AlmuraClient::maximizeOnFirstTick);
        }
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

    private static boolean maximized;

    // SDL window calls must run on the render thread (setup work runs on a worker and deadlocks), so wait for the first tick
    private static void maximizeOnFirstTick(final ClientTickEvent.Post event) {
        if (!maximized) {
            maximized = true;
            SDLVideo.SDL_MaximizeWindow(Minecraft.getInstance().getWindow().handle());
        }
    }

    private static void onRegisterPictureInPictureRenderers(final RegisterPictureInPictureRenderersEvent event) {
        // The animated Dockter_Almura figure of the title screen
        event.register(AlmuraFigureRenderState.class, AlmuraFigureRenderer::new);
    }
}
