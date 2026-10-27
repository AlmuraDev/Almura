/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.speed.AlmuraSettings;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.almuradev.almura.feature.menu.game.SimpleIngameMenu;
import com.almuradev.almura.feature.menu.main.DisconnectedGui;
import com.almuradev.almura.feature.menu.main.PanoramicMainMenu;
import com.almuradev.almura.feature.menu.main.WorldLoadingGui;
import com.almuradev.almura.feature.menu.singleplayer.SingleplayerScreen;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MainMenuManager implements Witness {
    @Inject private static MappedConfiguration<ClientConfiguration> configAdapter;
    boolean debug = false;

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        final GuiScreen screen = event.getGui();
        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        boolean refreshResources = false;

        if (screen != null) {
            if (currentScreen != null && debug) {
                System.out.println("MainMenuManager: current: " + currentScreen.getClass().getSimpleName() + " requested: " + screen.getClass().getSimpleName());
            }

            if (screen.getClass().equals(GuiMainMenu.class)) {
                event.setCanceled(true);

                final GeneralCategory general = configAdapter.get().general;
                if (general.firstLaunch) {
                    AlmuraSettings.checkFirstLaunched();
                } else {
                    new PanoramicMainMenu(null).display();
                }
            } else if (screen.getClass().equals(GuiWorldSelection.class)) {
                // Vanilla's world list, from wherever it's opened (e.g. Forge's world loader when its missing mods prompt is cancelled)
                event.setCanceled(true);
                new SingleplayerScreen(null).display();
            } else if (screen.getClass().equals(GuiIngameMenu.class)) {
                event.setCanceled(true);
                new SimpleIngameMenu().display();
            } else if (screen.getClass().equals(GuiDownloadTerrain.class)
                    || screen.getClass().equals(GuiScreenWorking.class) && Minecraft.getMinecraft().isIntegratedServerRunning()) {
                // Joining a world or changing dimension (single player or a server), and before that in single player, logging in to its own
                // server (vanilla's bare " 0%" screen). Keep the loading screen already showing rather than opening the same one again.
                event.setCanceled(true);
                if (!(currentScreen instanceof WorldLoadingGui)) {
                    new WorldLoadingGui().display();
                }
            } else if (screen.getClass().equals(GuiGameOver.class)) {
                event.setCanceled(true);
               // Cancel this, packet sent to client opens the DeathGUI
            } else if (screen.getClass().equals(GuiDisconnected.class)) {
                event.setCanceled(true);
                String message = "";

                if (Almura.networkManager != null && Almura.networkManager.getExitMessage() != null) {
                    message = Almura.networkManager.getExitMessage().getFormattedText();
                }

                new DisconnectedGui(message).display();
            }
        }
    }
}
