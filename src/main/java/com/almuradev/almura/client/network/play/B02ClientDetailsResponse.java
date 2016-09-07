/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.network.play;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;

public class B02ClientDetailsResponse implements IMessage, IMessageHandler<B02ClientDetailsResponse, B02ClientDetailsResponse> {

    public HashSet<String> names, modNames;

    public B02ClientDetailsResponse() {}

    public B02ClientDetailsResponse(HashSet<String> names, HashSet<String> modNames) {
        this.names = names;
        this.modNames = modNames;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (names != null) {
            buf.writeInt(names.size());

            for (String name : names) {
                ByteBufUtils.writeUTF8String(buf, name);
            }
            
        } else {
            buf.writeInt(0);
        }

        if (modNames != null) {
            buf.writeInt(modNames.size());

            for (String name : modNames) {
                ByteBufUtils.writeUTF8String(buf, name);
            }
        } else {
            buf.writeInt(0);
        }
    }

    @Override
    public B02ClientDetailsResponse onMessage(B02ClientDetailsResponse message, MessageContext ctx) {
        return null;
    }
}
