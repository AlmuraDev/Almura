/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.Page;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Instructs a client to open a {@link Page} immediately.
 */
public class S02PageOpen implements IMessage, IMessageHandler<S02PageOpen, IMessage> {

    public String identifier;

    public S02PageOpen() {
    }

    public S02PageOpen(String identifier) {
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
    public IMessage onMessage(S02PageOpen message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Almura.PROXY.handlePageOpen(ctx, message);
        }
        return null;
    }
}
