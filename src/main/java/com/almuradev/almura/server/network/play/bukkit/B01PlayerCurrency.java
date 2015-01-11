/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

import java.text.NumberFormat;
import java.util.Locale;

public class B01PlayerCurrency implements IMessage, IMessageHandler<B01PlayerCurrency, IMessage> {

    private static Locale caLoc = new Locale("en", "US");
    private static NumberFormat numForm = NumberFormat.getCurrencyInstance(caLoc);
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
            final String formatted = String.format(ChatColor.WHITE + numForm.format(message.amount));
            //final String[] money = Double.toString(message.amount).split("\\.");
            //String formatted = String.format(ChatColor.WHITE + "%s" + ChatColor.YELLOW + "g " + ChatColor.WHITE + "%s" + ChatColor.GRAY + "s", money.length > 0 ? money[0] : "0", money.length > 1 ? money[1] : "0");
            IngameHUD.INSTANCE.playerCurrency.setText(formatted);
        }
        return null;
    }
}
