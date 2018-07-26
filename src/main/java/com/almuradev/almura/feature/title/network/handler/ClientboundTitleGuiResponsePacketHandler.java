/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.client.gui.ManageTitlesGUI;
import com.almuradev.almura.feature.title.network.ClientboundTitleGuiResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundTitleGuiResponsePacketHandler implements MessageHandler<ClientboundTitleGuiResponsePacket> {

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundTitleGuiResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            new ManageTitlesGUI(message.isAdmin).display();
        }
    }
}

