/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.network.play;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class B03ChunkRegenWand implements IMessage, IMessageHandler<B03ChunkRegenWand, B03ChunkRegenWand> {    
    
    String player;
    
    public B03ChunkRegenWand() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public B03ChunkRegenWand onMessage(B03ChunkRegenWand message, MessageContext ctx) {
        return null;
    }
}
