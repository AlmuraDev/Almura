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

public class B01ResTokenConfirmation implements IMessage, IMessageHandler<B01ResTokenConfirmation, B00PlayerDisplayName> {
    public boolean useToken = false;    

    public B01ResTokenConfirmation() {

    }

    public B01ResTokenConfirmation(boolean useToken) {
        this.useToken = useToken;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        useToken = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(useToken);
    }

    @Override
    public B00PlayerDisplayName onMessage(B01ResTokenConfirmation message, MessageContext ctx) {
        return null;
    }
}
