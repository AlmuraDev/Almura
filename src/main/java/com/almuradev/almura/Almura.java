/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.server.ServerProxy;
import net.minecraftforge.fml.common.SidedProxy;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

@Plugin(id = Almura.PLUGIN_ID, name = Almura.PLUGIN_NAME, version = Almura.PLUGIN_VERSION)
public class Almura {

    public static final String PLUGIN_ID = "almura", PLUGIN_NAME = "Almura", PLUGIN_VERSION = "1.8.9-1694-r3.1",
        GUI_VERSION = "b200", PACK_VERSION = "1.5";

    private static Almura instance;

    public static Almura getInstance() {
        checkNotNull(instance, "Almura has not been loaded yet!");
        return instance;
    }

    @SidedProxy(clientSide = ClientProxy.CLASSPATH, serverSide = ServerProxy.CLASSPATH)
    public static CommonProxy proxy;

    @Inject public Logger logger;
    @Inject public PluginContainer container;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        instance = this;
        proxy.onGamePreInitialization(event);
    }
}
