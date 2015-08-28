/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.network.play;

import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class B00PlayerDeathConfirmation implements IMessage, IMessageHandler<B00PlayerDeathConfirmation, B00PlayerDisplayName> {
    public boolean acceptsRespawnPenalty = false;

    public B00PlayerDeathConfirmation() {

    }

    public B00PlayerDeathConfirmation(boolean acceptsRespawnPenalty) {
        this.acceptsRespawnPenalty = acceptsRespawnPenalty;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        acceptsRespawnPenalty = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(acceptsRespawnPenalty);
    }

    @Override
    public B00PlayerDisplayName onMessage(B00PlayerDeathConfirmation message, MessageContext ctx) {
        return null;
    }
}
