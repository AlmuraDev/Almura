/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ClientProxy;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class B00PlayerDisplayName implements IMessage, IMessageHandler<B00PlayerDisplayName, IMessage> {

    public String username, displayName;

    public B00PlayerDisplayName() {
    }

    public B00PlayerDisplayName(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        username = ByteBufUtils.readUTF8String(buf);
        displayName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, username);
        ByteBufUtils.writeUTF8String(buf, displayName);
    }

    @Override
    public IMessage onMessage(B00PlayerDisplayName message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            final EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(message.username);
            if (player != null) {
                ClientProxy.PLAYER_DISPLAY_NAME_MAP.put(message.username, message.displayName);
                player.refreshDisplayName();
            }
        }
        return null;
    }
}
