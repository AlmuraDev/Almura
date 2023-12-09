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
    public static String buildNumber = "b113";

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

    @Listener
    public void onWorldUnload(final UnloadWorldEvent event) {
        // This is to prevent worlds from unloading after they have loaded once.
        // Make sure sponge world.conf file includes:
        // # If 'true', this world will load on startup.
        // load-on-startup=true
		// # If 'true', this worlds spawn will remain loaded with no players.
        // keep-spawn-loaded=false
        // This allows all chunks to unload and not tick but prevents the natural unload process.
        // On Almura, if a world unloads and reloads the TerrainControl config becomes contaminated.
        System.out.println("Almura: Attempted to unload:[" + event.getTargetWorld().getName()+ "] but cancelled it, this is ignored on shutdown.");
        event.setCancelled(true);
    }
}
