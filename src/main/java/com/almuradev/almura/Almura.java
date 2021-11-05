/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.feature.claim.ClaimModule;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiRequestPacketHandler;
import com.google.inject.Injector;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
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

    @Listener(order = Order.POST)
    public void gamePreInit(final GamePreInitializationEvent event) {
        proxy.preInit();
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
        // The following prevents worlds from unloading unless server is shutting down
        // This is necessary to prevent TerrainControl configs loading more than once
        // on the dev server.  Live server doesn't have this issue because each world
        // is kept active by Dynmap holding 1 chunk open per loaded world.
        if (!isShuttingDown) {
            System.out.println("Almura prevented unloading of: " + event.getTargetWorld().getName());
            event.setCancelled(true);
        }
    }
}
