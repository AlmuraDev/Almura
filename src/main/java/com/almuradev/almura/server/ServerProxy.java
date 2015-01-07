/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

public class ServerProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";

    @Override
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //Almura.NETWORK_FORGE.sendTo(new S00AdditionalWorldInfo(event.player.worldObj.getWorldInfo().getWorldName(), 0, 50), (EntityPlayerMP) event.player);
    	// Above line not needed because bridge is sending this.
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.fromDim != event.toDim) {
            //Almura.NETWORK_FORGE.sendTo(new S00AdditionalWorldInfo(event.player.worldObj.getWorldInfo().getWorldName(), 0, 50), (EntityPlayerMP) event.player);
        }
    }
}
