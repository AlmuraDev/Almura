/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.content.Page;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.server.network.play.S00PageInformation;
import com.almuradev.almura.server.network.play.S01PageDelete;
import com.almuradev.almura.server.network.play.S02PageOpen;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Map;

public class ServerProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";

    @Override
    public void onInitialization(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    public void handlePageDelete(MessageContext ctx, S01PageDelete message) {
        super.handlePageDelete(ctx, message);

        CommonProxy.NETWORK_FORGE.sendToAll(new S01PageDelete(message.identifier));
    }

    @Override
    public boolean canSavePages() {
        return true;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        for (Map.Entry<String, Page> entry : PageRegistry.getAll().entrySet()) {
            CommonProxy.NETWORK_FORGE.sendTo(new S00PageInformation(entry.getValue()), (EntityPlayerMP) event.player);
            // TODO This is how you open a gui on their client, replace "byes" with the page's identifier
            CommonProxy.NETWORK_FORGE.sendTo(new S02PageOpen("Welcome"), (EntityPlayerMP) event.player);
        }
    }
}
