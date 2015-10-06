/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.network.play;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class B00PlayerDeathConfirmation implements IMessage, IMessageHandler<B00PlayerDeathConfirmation, B00PlayerDeathConfirmation> {
    public boolean acceptsRespawnPenalty = false;
    public int x, y, z;
    public String world;

    public B00PlayerDeathConfirmation() {

    }

    public B00PlayerDeathConfirmation(boolean acceptsRespawnPenalty, int x, int y, int z, String world) {
        this.acceptsRespawnPenalty = acceptsRespawnPenalty;
        this.x = x;
        this.y = y;
        this.z = z;        
        this.world = world;
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
    public B00PlayerDeathConfirmation onMessage(B00PlayerDeathConfirmation message, MessageContext ctx) {
        return null;
    }
}
