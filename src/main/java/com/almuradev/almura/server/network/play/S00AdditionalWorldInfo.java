/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class S00AdditionalWorldInfo implements IMessage, IMessageHandler<S00AdditionalWorldInfo, IMessage> {

    public String worldName;
    public int currentPlayers, maxPlayers;

    public S00AdditionalWorldInfo() {
    }

    public S00AdditionalWorldInfo(String worldName, int currentPlayers, int maxPlayers) {
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

    @Override
    public IMessage onMessage(S00AdditionalWorldInfo message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            if (message.worldName.equalsIgnoreCase("Dim1")) {
                IngameHUD.INSTANCE.worldDisplay.setText("The End");
            } else if (message.worldName.equalsIgnoreCase("Dim-1")) {
                IngameHUD.INSTANCE.worldDisplay.setText("Nether");
            } else if (message.worldName.equalsIgnoreCase("redrock_nether")) {
                IngameHUD.INSTANCE.worldDisplay.setText("Redrock Nether");
            } else {
                IngameHUD.INSTANCE.worldDisplay.setText(Character.toUpperCase(message.worldName.charAt(0)) + message.worldName.substring(1));
            }
            System.out.println("Packet Recieved: " + message.currentPlayers + " / " + message.maxPlayers);
            IngameHUD.INSTANCE.serverCount.setText(message.currentPlayers + "/" + message.maxPlayers);
        }
        return null;
    }
}
