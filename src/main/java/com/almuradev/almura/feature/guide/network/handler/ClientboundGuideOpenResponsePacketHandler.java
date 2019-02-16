/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.client.gui.GuidePageViewScreen;
import com.almuradev.almura.feature.guide.network.ClientboundGuideOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.GuideOpenType;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundGuideOpenResponsePacketHandler implements MessageHandler<ClientboundGuideOpenResponsePacket> {

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundGuideOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                if (message.type == GuideOpenType.PLAYER_LOGGED_IN && (!Minecraft.getMinecraft().isIntegratedServerRunning())) {
                    new GuidePageViewScreen(message.canAdd, message.canRemove, message.canModify).display();
                } else if (message.type != GuideOpenType.PLAYER_LOGGED_IN) {
                    new GuidePageViewScreen(message.canAdd, message.canRemove, message.canModify).display();
                }
            }
        }
    }
}

