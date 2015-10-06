/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.DisplayNameManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class B00PlayerDisplayName implements IMessage, IMessageHandler<B00PlayerDisplayName, IMessage> {

    public String username, displayName;

    public B00PlayerDisplayName() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        username = ByteBufUtils.readUTF8String(buf);
        displayName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B00PlayerDisplayName message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            final String[] split = message.displayName.split("\\n");
            DisplayNameManager.putDisplayName(message.username, split[0]);
            if (split.length > 1) {
                DisplayNameManager.putTitle(message.username, split[1]);
            }
            // If this client already has a Player object for this username, tell Forge to refresh its displayname
            final EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(message.username);
            if (player != null) {
                player.refreshDisplayName();
            }
        }
        return null;
    }
}
