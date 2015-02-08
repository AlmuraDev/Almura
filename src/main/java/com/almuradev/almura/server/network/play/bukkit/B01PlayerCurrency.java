/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

import java.text.NumberFormat;
import java.util.Locale;

public class B01PlayerCurrency implements IMessage, IMessageHandler<B01PlayerCurrency, IMessage> {
    private static final Locale LOCALE_EN = new Locale("en", "US");
    private static final NumberFormat FORMAT_NUMBER_EN = NumberFormat.getCurrencyInstance(LOCALE_EN);
    public double amount;

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B01PlayerCurrency message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ClientProxy.HUD_INGAME.playerCurrency.setText(String.format(ChatColor.WHITE + FORMAT_NUMBER_EN.format(message.amount)));
        }
        return null;
    }
}
