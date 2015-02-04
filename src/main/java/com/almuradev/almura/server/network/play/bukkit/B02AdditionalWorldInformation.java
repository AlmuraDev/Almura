/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class B02AdditionalWorldInformation implements IMessage, IMessageHandler<B02AdditionalWorldInformation, IMessage> {

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
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B02AdditionalWorldInformation message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ClientProxy.HUD_INGAME.worldDisplay.setText(Character.toUpperCase(message.worldName.charAt(0)) + message.worldName.substring(1));
            ClientProxy.HUD_INGAME.serverCount.setText(message.currentPlayers + "/" + message.maxPlayers);
        }
        return null;
    }
}
