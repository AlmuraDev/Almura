/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class B00PlayerDisplayName implements IMessage, IMessageHandler<B00PlayerDisplayName, IMessage> {
    public String displayName;

    public B00PlayerDisplayName() {
    }

    public B00PlayerDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        displayName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, displayName);
    }

    @Override
    public IMessage onMessage(B00PlayerDisplayName message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            IngameHUD.INSTANCE.playerTitle.setText(message.displayName);
        }
        return null;
    }
}
