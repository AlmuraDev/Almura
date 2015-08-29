/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.network.play;

import cpw.mods.fml.common.network.ByteBufUtils;

import io.netty.buffer.Unpooled;

import java.nio.Buffer;
import java.nio.ByteBuffer;


import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class B00PlayerDeathConfirmation implements IMessage, IMessageHandler<B00PlayerDeathConfirmation, B00PlayerDisplayName> {
    public boolean acceptsRespawnPenalty = false;
    private int x, y, z;
    private String world;

    public B00PlayerDeathConfirmation() {

    }

    public B00PlayerDeathConfirmation(boolean acceptsRespawnPenalty, int x, int y, int z, String world) {
        this.acceptsRespawnPenalty = acceptsRespawnPenalty;
        this.x = x;
        this.y = y;
        this.z = z;        
        this.world = world;
        System.out.println("World to Packet: " + world);
        ByteBuf buffer = Unpooled.buffer();
        toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        acceptsRespawnPenalty = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(acceptsRespawnPenalty);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, this.world);
    }

    @Override
    public B00PlayerDisplayName onMessage(B00PlayerDeathConfirmation message, MessageContext ctx) {
        return null;
    }
}
