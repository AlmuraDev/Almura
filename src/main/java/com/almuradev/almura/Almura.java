/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
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
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {

    public static final Pack INTERNAL_PACK = new Pack("internal");
    public static final String MOD_ID = "almura";
    public static final Logger LOGGER = LogManager.getLogger(Almura.MOD_ID);
    public static final SimpleNetworkWrapper NETWORK_FORGE = new SimpleNetworkWrapper("AM|FOR");
    public static final SimpleNetworkWrapper NETWORK_BUKKIT = new SimpleNetworkWrapper("AM|BUK");
    public static String GUI_VERSION = "Build 51";
    public static String PACK_VERSION = "1.3";

    @Instance
    public static Almura INSTANCE;

    @SidedProxy(clientSide = ClientProxy.CLASSPATH, serverSide = ServerProxy.CLASSPATH)
    public static CommonProxy PROXY;

    @EventHandler
    public void onPreInitializationEvent(FMLPreInitializationEvent event) {
        PROXY.onPreInitialization(event);
    }

    @EventHandler
    public void onInitializationEvent(FMLInitializationEvent event) {
        PROXY.onInitialization(event);
    }
}
