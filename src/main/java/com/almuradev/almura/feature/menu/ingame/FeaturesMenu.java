/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.menu.ingame.network.FeaturesMessages.ClientboundOpen;
import com.almuradev.almura.feature.menu.ingame.network.FeaturesMessages.ServerboundOpen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The Almura Features menu, opened in game with F12 (a key the player can change, under "Almura Features Menu" in the controls), as on 1.12.
 *
 * <p>On 1.12 the server decided whether the player may open it ({@code almura.feature.base}) and whether they see the administrator part
 * ({@code almura.feature.admin}). Until 4.0's server side is ported it opens for everyone, with the administrator part for operators.</p>
 */
public final class FeaturesMenu {

    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Almura.ID, "features"));
    private static final KeyMapping OPEN = new KeyMapping("key.almura.features.open", InputConstants.KEY_F12, CATEGORY);

    private FeaturesMenu() {
    }

    public static void register(final IEventBus modBus) {
        modBus.addListener(FeaturesMenu::onRegisterKeys);
        NeoForge.EVENT_BUS.register(new FeaturesMenu());
    }

    private static void onRegisterKeys(final RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN);
    }

    @SubscribeEvent
    public void onClientTick(final ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            open();
        }
    }

    private static void open() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) {
            return;
        }
        // The features live on Almura's server; in a released build single-player has none, as on 1.12
        if (FMLEnvironment.isProduction() && minecraft.isLocalServer()) {
            minecraft.gui.hud.getChat().addClientSystemMessage(Component.literal("This is only available on the Almura server, not in singleplayer."));
            return;
        }
        // As 3.5: the server says whether the player may open it (almura.feature.base) and sees its administrator buttons
        // (almura.feature.admin)
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    /**
     * The server's answer to F12: the menu, with the administrator buttons or not.
     */
    public static void onOpen(final ClientboundOpen message) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.gui.screen() == null) {
            InGameScreens.open(new FeaturesScreen(message.admin()), null);
        }
    }
}
