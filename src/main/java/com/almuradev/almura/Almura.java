/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.lang.LanguageManager;
import com.almuradev.almura.server.ServerProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {
    public static final String MOD_ID = "almura";
    public static final Logger LOGGER = LogManager.getLogger("almura");
    public static final LanguageManager LANGUAGES = new LanguageManager();
    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(Almura.MOD_ID);
    public static String GUI_VERSION = "Almura 2.0 build 14 alpha";
    public static String PACK_VERSION = "Content Pack 1";

    @SidedProxy(clientSide = ClientProxy.CLASSPATH, serverSide = ServerProxy.CLASSPATH)
    public static CommonProxy PROXY;

    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        PROXY.onPreInitialization(event);
    }

    @EventHandler
    public void onPostInitializationEvent(FMLPostInitializationEvent event) {
        PROXY.onPostInitialization(event);
    }
}
