/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.gui.ingame.HUDData;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class B02AdditionalWorldInformation implements IMessage, IMessageHandler<B02AdditionalWorldInformation, IMessage> {

    public String worldDisplayName, worldName;
    public int currentPlayers, maxPlayers, permissionLevel;

    @Override
    public void fromBytes(ByteBuf buf) {
        worldDisplayName = ByteBufUtils.readUTF8String(buf);
        worldName = ByteBufUtils.readUTF8String(buf);
        currentPlayers = buf.readInt();
        maxPlayers = buf.readInt();
        permissionLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B02AdditionalWorldInformation message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            HUDData.WORLD_DISPLAY = message.worldDisplayName;
            HUDData.WORLD_NAME = message.worldName;
            HUDData.SERVER_COUNT = message.currentPlayers + "/" + message.maxPlayers;
            HUDData.PERMISSION_LEVEL = message.permissionLevel;
        }
        return null;
    }
}
