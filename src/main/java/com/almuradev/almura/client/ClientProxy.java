/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.client.gui.menu.DynamicMainMenu;
import com.almuradev.almura.network.NetworkHandlers;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

/**
 * The client platform of Almura. All code not meant to run on a dedicated server should go here.
 */
public final class ClientProxy extends CommonProxy {
    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";

    @Override
    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiScreen(GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass().equals(GuiMainMenu.class)) {
            event.setCanceled(true);
            new DynamicMainMenu(null).display();
        }
    }

    @Override
    protected void registerMessages() {
        super.registerMessages();
        this.network.addHandler(SWorldInformationMessage.class, Platform.Type.CLIENT, new NetworkHandlers.S00WorldInformationHandler());
    }
}
