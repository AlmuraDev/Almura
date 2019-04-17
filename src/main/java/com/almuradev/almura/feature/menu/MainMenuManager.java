/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.feature.death.client.gui.PlayerDiedGUI;
import com.almuradev.almura.feature.menu.ingame.IngameMenu;
import com.almuradev.almura.feature.menu.main.MainMenu;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MainMenuManager implements Witness {

    boolean debug = false;

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        final GuiScreen screen = event.getGui();
        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            if (currentScreen != null && debug) {
                System.out.println("MainMenuManager: current: " + currentScreen.getClass().getSimpleName() + " requested: " + screen.getClass().getSimpleName());
            }

            if (screen.getClass().equals(GuiMainMenu.class)) {
                event.setCanceled(true);
                new MainMenu().display();
            } else if (screen.getClass().equals(GuiIngameMenu.class)) {
                event.setCanceled(true);
                new IngameMenu().display();
            } else if (screen.getClass().equals(GuiGameOver.class)) {
                event.setCanceled(true);
                if (currentScreen == null ||(currentScreen != null && !currentScreen.getClass().equals(PlayerDiedGUI.class))) {
                    // Handled within a listener in DeathHandler.class
                    //new PlayerDiedGUI(Minecraft.getMinecraft().player).display();
                }
                if (currentScreen != null && currentScreen.getClass().equals(PlayerDiedGUI.class)) {
                    // Ignore request, something internally tries to fire this more than once.
                }
            }
        }
    }
}
