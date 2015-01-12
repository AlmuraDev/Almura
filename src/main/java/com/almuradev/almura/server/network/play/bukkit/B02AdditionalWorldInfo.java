/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class B02AdditionalWorldInfo implements IMessage, IMessageHandler<B02AdditionalWorldInfo, IMessage> {

    public String worldName;
    public int currentPlayers, maxPlayers;

    @Override
    public void fromBytes(ByteBuf buf) {
        worldName = ByteBufUtils.readUTF8String(buf);
        currentPlayers = buf.readInt();
        maxPlayers = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, worldName);
        buf.writeInt(currentPlayers);
        buf.writeInt(maxPlayers);
    }

    @Override
    public IMessage onMessage(B02AdditionalWorldInfo message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            IngameHUD.INSTANCE.worldDisplay.setText(Character.toUpperCase(message.worldName.charAt(0)) + message.worldName.substring(1));
            IngameHUD.INSTANCE.serverCount.setText(message.currentPlayers + "/" + message.maxPlayers);
        }
        return null;
    }
}
