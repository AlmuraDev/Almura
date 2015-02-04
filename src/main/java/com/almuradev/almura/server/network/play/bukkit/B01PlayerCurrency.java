/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class B01PlayerCurrency implements IMessage, IMessageHandler<B01PlayerCurrency, IMessage> {

    public String formattedCurrency;

    @Override
    public void fromBytes(ByteBuf buf) {
        formattedCurrency = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B01PlayerCurrency message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ClientProxy.HUD_INGAME.playerCurrency.setText(formattedCurrency);
        }
        return null;
    }
}
