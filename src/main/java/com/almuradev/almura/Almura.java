/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.google.inject.Injector;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.SidedProxy;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(
    id = Almura.ID,
    dependencies = {
        @Dependency(id = "nucleus", optional = true),
        @Dependency(id = "griefdefender", optional = true),
        @Dependency(id = "terraincontrol", optional = true)
    }
)
public class Almura implements com.almuradev.almura.shared.plugin.Plugin {

    public static final String ID = "almura";
    public static final String NAME = "Almura";
    public static boolean debug = true;
    public static boolean isShuttingDown = false;
    public static NetworkManager networkManager;
    public static String buildNumber = "b104";

    @SidedProxy(
            modId = ID,
            clientSide = "com.almuradev.almura.core.client.ClientProxy",
            serverSide = "com.almuradev.almura.core.server.DedicatedServerProxy"
    )
    private static CommonProxy proxy;
    @Inject private Injector injector;

    @Listener(order = Order.LATE)
    public void gameConstruct(final GameConstructionEvent event) {
        //Note: proxy construct creation has to take place on this event because later phases
        // won't allow the content builder materials to register properly.
        System.out.println("Creating Almura system level injectors !!!");
        proxy.construct(this, this.injector);
    }

    @Listener
    public void gameDestruct(final GameStoppingEvent event) {
        proxy.destruct();
    }

    @Listener
    public void gameShutdown(final GameStoppingServerEvent event) {
        isShuttingDown = true;
    }
}
