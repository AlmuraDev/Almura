/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.ClientProxy;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class S00AdditionalWorldInformation implements IMessage, IMessageHandler<S00AdditionalWorldInformation, IMessage> {

    public String worldName;
    public int currentPlayers, maxPlayers;

    public S00AdditionalWorldInformation() {
    }

    public S00AdditionalWorldInformation(String worldName, int currentPlayers, int maxPlayers) {
        this.worldName = worldName;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

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

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S00AdditionalWorldInformation message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ClientProxy.HUD_INGAME.serverCount.setText(message.currentPlayers + "/" + message.maxPlayers);
        }
        return null;
    }
}
