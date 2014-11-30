/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class B01PlayerCurrency implements IMessage, IMessageHandler<B01PlayerCurrency, IMessage> {

    public double amount;

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(amount);
    }

    @Override
    public IMessage onMessage(B01PlayerCurrency message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            //TODO Need the GUI Widget for the currency amount currently
        }
        return null;
    }
}
