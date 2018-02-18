/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundGuideOpenResponsePacketHandler implements MessageHandler<ClientboundGuideOpenResponsePacket> {

    /* Message Types
    1 = onPlayerLogin
    2 = Client-Side onKeyTyped
    3 = Forced open via command via different player.
     */

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundGuideOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (message.type == 1 && (!Minecraft.getMinecraft().isIntegratedServerRunning())) {  // Request from ServerManager onLogin && ignore Single Player
                new SimplePageView(message.canAdd, message.canRemove, message.canModify).display();
            } else if (message.type >= 2) {
                new SimplePageView(message.canAdd, message.canRemove, message.canModify).display();
            }
        }
    }
}

