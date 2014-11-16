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

public class ServerProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";

    @Override
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);

        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Almura.NETWORK.sendTo(new S00AdditionalWorldInfo(event.player.worldObj.getWorldInfo().getWorldName()), (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.fromDim != event.toDim) {
            Almura.NETWORK.sendTo(new S00AdditionalWorldInfo(event.player.worldObj.getWorldInfo().getWorldName()), (EntityPlayerMP) event.player);
        }
    }
}
