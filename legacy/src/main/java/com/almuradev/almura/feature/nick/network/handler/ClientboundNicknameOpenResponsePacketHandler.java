/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.feature.nick.client.gui.NicknameGUI;
import com.almuradev.almura.feature.nick.network.ClientboundNicknameOpenResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundNicknameOpenResponsePacketHandler implements MessageHandler<ClientboundNicknameOpenResponsePacket> {

    @Override
    public void handleMessage(ClientboundNicknameOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            new NicknameGUI(Minecraft.getMinecraft().player, message.canChangeNickname, message.isAdmin).display();
        }
    }
}
