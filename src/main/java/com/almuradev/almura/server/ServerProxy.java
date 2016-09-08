/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.content.Page;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.server.network.play.S00PageInformation;
import com.almuradev.almura.server.network.play.S01PageDelete;
import com.almuradev.almura.server.network.play.S02PageOpen;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Map;

public class ServerProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";
    private int tickCounter;

    @Override
    public void handlePageDelete(MessageContext ctx, S01PageDelete message) {
        super.handlePageDelete(ctx, message);

        CommonProxy.NETWORK_FORGE.sendToAll(new S01PageDelete(message.identifier));
    }

    @Override
    public boolean canSavePages() {
        return true;
    }

    @Override
    public void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        super.handlePlayerLoggedIn(event);

        for (Map.Entry<String, Page> entry : PageRegistry.getAll().entrySet()) {
            CommonProxy.NETWORK_FORGE.sendTo(new S00PageInformation(entry.getValue()), (EntityPlayerMP) event.player);
            CommonProxy.NETWORK_FORGE.sendTo(new S02PageOpen("Welcome"), (EntityPlayerMP) event.player);
        }
    }
    
    @SubscribeEvent
    public void ServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (this.tickCounter % 40 == 0) {
                //System.out.println("Events: " + event.getListenerList().toString());
            }
            ++this.tickCounter;
        }
    }
}
