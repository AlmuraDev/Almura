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
import com.almuradev.almura.feature.accessory.client.ClientAccessories;
import com.almuradev.almura.feature.armor.client.HelmetLights;
import com.almuradev.almura.feature.almanac.client.CropStageRenderer;
import com.almuradev.almura.feature.almanac.client.CropStageRenderState;
import com.almuradev.almura.feature.cache.Caches;
import com.almuradev.almura.feature.cache.client.CacheRenderer;
import com.almuradev.almura.feature.death.client.ClientDeath;
import com.almuradev.almura.feature.harvestsack.HarvestingSack;
import com.almuradev.almura.feature.healthbar.client.EntityLabels;
import com.almuradev.almura.feature.hud.Huds;
import com.almuradev.almura.feature.hud.ServerHudData;
import com.almuradev.almura.feature.lighting.client.ClientFlickerLights;
import com.almuradev.almura.feature.lighting.client.FlickerLightRenderer;
import com.almuradev.almura.feature.machine.Machines;
import com.almuradev.almura.feature.machine.client.ProcessingScreen;
import com.almuradev.almura.feature.machine.client.SpinningPartRenderer;
import com.almuradev.almura.feature.menu.Disconnected;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderer;
import com.almuradev.almura.feature.menu.figure.AlmuraFigureRenderState;
import com.almuradev.almura.feature.menu.figure.FigureSlot;
import com.almuradev.almura.feature.menu.GameMenu;
import com.almuradev.almura.feature.menu.ingame.FeaturesMenu;
import com.almuradev.almura.feature.menu.MainMenuManager;
import com.almuradev.almura.feature.menu.options.AlmuraOptions;
import com.almuradev.almura.feature.menu.ServerStatus;
import com.almuradev.almura.feature.menu.WorldLoading;
import com.almuradev.almura.feature.nick.client.ClientNicks;
import com.almuradev.almura.feature.notification.client.HiddenToasts;
import com.almuradev.almura.feature.offhand.client.OffhandTorches;
import com.almuradev.almura.feature.title.client.ClientTitles;
import com.almuradev.almura.feature.title.client.PlayerPreview;
import com.almuradev.almura.feature.title.client.TitleTags;
import com.almuradev.almura.feature.worldedit.client.ClientWorldEditSelection;
import com.almuradev.almura.feature.worldload.client.ClientWorldLoad;
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
        // The health bars' health, kept on living entities' render states
        modBus.addListener(EntityLabels::onRegisterRenderStateModifiers);
        // Accessories: on players' render states, drawn as a layer of the player renderer
        modBus.addListener(ClientAccessories::onRegisterRenderStateModifiers);
        modBus.addListener(ClientAccessories::onAddLayers);
        modBus.addListener(AlmuraClient::onRegisterPictureInPictureRenderers);
        // The kitchen machines' spinning parts, which no block state names
        modBus.addListener(SpinningPartRenderer::onRegisterStandalone);
        // Almura's lettering, above any resource pack's
        AlmuraFontPack.register(modBus);
        // The storage barrel's label
        modBus.addListener((final EntityRenderersEvent.RegisterRenderers event) -> {
            // A cache shows what it holds on its front
            event.registerBlockEntityRenderer(Caches.blockEntityType(), CacheRenderer::new);
            if (ContentContainers.blockEntityType() != null) {
                event.registerBlockEntityRenderer(ContentContainers.blockEntityType(),
                    StorageBarrelRenderer::new);
            }
            // A flickering light's fast blink
            if (FlickerLights.blockEntityType() != null) {
                event.registerBlockEntityRenderer(FlickerLights.blockEntityType(),
                    FlickerLightRenderer::new);
            }
            // The kitchen machines' blade and cutting plate
            event.registerBlockEntityRenderer(Machines.blockEntityType(), SpinningPartRenderer::new);
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
            // Almura's machines show 3.5's machine screen
            if (Machines.menuTypeOrNull() != null) {
                event.register(Machines.menuType(), ProcessingScreen::new);
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
        NeoForge.EVENT_BUS.register(new ClientAccessories.Resetter());
        NeoForge.EVENT_BUS.register(new ClientAccessories.Labels());
        // Names and health bars over players, monsters and animals, and the off-hand torch option
        NeoForge.EVENT_BUS.register(new EntityLabels());
        NeoForge.EVENT_BUS.register(new OffhandTorches());
        // The lighted helmet's light, drawn by the client alone
        NeoForge.EVENT_BUS.register(new HelmetLights());
        // Titles over players' names, nicknames on them, and the Titles and Nicknames windows' player
        NeoForge.EVENT_BUS.register(new TitleTags());
        NeoForge.EVENT_BUS.register(new ClientTitles.Resetter());
        NeoForge.EVENT_BUS.register(new ClientNicks.Hooks());
        NeoForge.EVENT_BUS.register(new ClientDeath.Hooks());
        NeoForge.EVENT_BUS.register(new ClientWorldLoad.Hooks());
        // No "Chat messages can't be verified" toast on joining a server that can't verify chat
        NeoForge.EVENT_BUS.register(new HiddenToasts());
        NeoForge.EVENT_BUS.register(new PlayerPreview.Drawer());
        // The player's WorldEdit selection, drawn in the world when the server has WorldEdit
        ClientWorldEditSelection.register(modBus);
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
        // The Farmer's Almanac's crop growth stages
        event.register(CropStageRenderState.class, CropStageRenderer::new);
    }
}
