/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.AlmuraMainMenu;
import com.almuradev.almura.lang.LanguageManager;
import com.almuradev.almura.server.ServerProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {

    public static final String MOD_ID = "almura";
    public static final Logger LOGGER = LogManager.getLogger("almura");
    public static final LanguageManager LANGUAGES = new LanguageManager();
    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(Almura.MOD_ID);
    public static String VERSION = "Almura 2.0 build 10 alpha";

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
