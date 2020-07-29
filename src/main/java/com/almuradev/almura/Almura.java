/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.google.inject.Injector;
import com.griefdefender.api.GriefDefender;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.SidedProxy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
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
    public static NetworkManager networkManager;

    @SidedProxy(
            modId = ID,
            clientSide = "com.almuradev.almura.core.client.ClientProxy",
            serverSide = "com.almuradev.almura.core.server.DedicatedServerProxy"
    )
    private static CommonProxy proxy;
    @Inject private Injector injector;

    @Listener
    public void gameConstruct(final GameConstructionEvent event) {
        proxy.construct(this, this.injector);
    }

    @Listener
    public void gameDestruct(final GameStoppingEvent event) {
        proxy.destruct();
    }

    @Listener
    public void gameStartedServer(final GameStartedServerEvent event) {
        if (Sponge.getPluginManager().isLoaded("griefdefender")) {
            final ServerClaimManager manager = this.injector.getInstance(ServerClaimManager.class);
            System.out.println("Registering GD Event Listeners within Almura");
            GriefDefender.getEventManager().register(manager);
        }
    }

    @Listener
    public void onWorldUnload(final UnloadWorldEvent event) {
        // The following prevents worlds from unloading unless server is shutting down
        // This is necessary to prevent TerrainControl configs loading more than once
        // on the dev server.  Live server doesn't have this issue because each world
        // is kept active by Dynmap holding 1 chunk open per loaded world.
        System.out.println("Almura prevented unloading of: " + event.getTargetWorld().getName());
        //event.setCancelled(true);
    }
}
