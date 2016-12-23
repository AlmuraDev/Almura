/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import net.minecraftforge.fml.common.SidedProxy;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

@Plugin(id = Constants.Plugin.ID)
public class Almura {

    public static Almura instance;

    @SidedProxy(modId = Constants.Plugin.ID, clientSide = Constants.Plugin.PROXY_CLIENT_CLASSPATH,
            serverSide = Constants.Plugin.PROXY_SERVER_CLASSPATH)
    public static CommonProxy proxy;

    @Inject public Logger logger;
    @Inject public PluginContainer container;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;

        proxy.onGameConstruction(event);
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        proxy.onGamePreInitialization(event);
    }
}
