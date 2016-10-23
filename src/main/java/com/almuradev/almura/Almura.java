/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.server.ServerProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID, name = "Almura", version = "1.7.10-1492", dependencies = "after:IC2@[2.2.770-experimental,)")
public class Almura {

    public static final Pack INTERNAL_PACK = new Pack("internal");
    public static final String MOD_ID = "almura";
    public static final Logger LOGGER = LogManager.getLogger(Almura.MOD_ID);
    public static String GUI_VERSION = "b122";
    public static String PACK_VERSION = "1.4";
    public static String FORGE_VERSION = "1.7.10-1614";

    @Instance
    public static Almura INSTANCE;

    @SidedProxy(clientSide = ClientProxy.CLASSPATH, serverSide = ServerProxy.CLASSPATH)
    public static CommonProxy PROXY;

    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        PROXY.onPreInitialization(event);
    }

    @EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        PROXY.onInitialization(event);
    }

    @EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        PROXY.onLoadComplete(event);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        PROXY.onServerStarting(event);
    }
}
