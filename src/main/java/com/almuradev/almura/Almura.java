/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.blocks.Blocks;
import com.almuradev.almura.client.Bindings;
import com.almuradev.almura.client.gui.AlmuraMainMenu;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import com.almuradev.almura.items.Items;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {

    public static final String MOD_ID = "Almura";
    public static final Logger LOGGER = LogManager.getLogger("Almura");
    public static String VERSION_STRING = "Almura 2.0 build 1 alpha";

    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        if (Configuration.IS_DEBUG) {
            LOGGER.info("Debug-mode toggled ON");
        }
        Blocks.fakeStaticLoad();
        Items.fakeStaticLoad();
        Tabs.fakeStaticLoad();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            setupClientResources();       
        }
    }

    @EventHandler
    public void onPostInitializationEvent(FMLPostInitializationEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new IngameHUD());
            MinecraftForge.EVENT_BUS.register(new IngameDebugHUD());
        }
    }

    @SideOnly(Side.CLIENT)
    private void setupClientResources() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }
    

//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public void onKeyPress(InputEvent.KeyInputEvent event) {
//    }
       
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui != null) {
            if (event.gui instanceof GuiMainMenu) {
                event.setCanceled(true);
                final AlmuraMainMenu box = new AlmuraMainMenu();
                box.display();
            }
        } 
    }    
}
