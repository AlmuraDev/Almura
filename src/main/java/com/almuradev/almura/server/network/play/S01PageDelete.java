/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.Almura;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class S01PageDelete implements IMessage, IMessageHandler<S01PageDelete, IMessage> {

    public String identifier;

    public S01PageDelete() {
    }

    public S01PageDelete(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        identifier = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, identifier);
    }

    @Override
    public IMessage onMessage(S01PageDelete message, MessageContext ctx) {
        Almura.PROXY.handlePageDelete(ctx, message);
        return null;
    }
}
