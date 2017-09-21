/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.google.inject.Injector;
import net.minecraftforge.fml.common.SidedProxy;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

@Plugin(id = Constants.Plugin.ID)
public class Almura {

    public static Almura instance;

    @SidedProxy(
            modId = Constants.Plugin.ID,
            clientSide = Constants.Plugin.PROXY_CLIENT_CLASSPATH,
            serverSide = Constants.Plugin.PROXY_SERVER_CLASSPATH
    )
    public static CommonProxy proxy;

    @Inject private Injector injector;
    @Inject public Logger logger;
    @Inject public PluginContainer container;

    public Almura() {
        instance = this;
    }

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        proxy.construct(this.injector);
        proxy.onGameConstruction(event);
    }

    @Listener
    public void gameStopping(final GameStoppingEvent event) {
        proxy.destruct();
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        proxy.onGamePreInitialization(event);
    }
}
