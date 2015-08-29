/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class B05GuiController implements IMessage, IMessageHandler<B05GuiController, IMessage> {

    public int guiId;
    public int defaultButton;
    public String username;
    
    @Override
    public void fromBytes(ByteBuf buf) {
        guiId = buf.readInt();
        defaultButton = buf.readInt();
        username = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public IMessage onMessage(B05GuiController message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            final EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(message.username);
            if (player != null) {
                // Residence Token Confirmation GUI
                if (guiId == 1) {
                    //new ResTokenConfirmation(null).display();  //Default button would be an extra method within the GUI's constructor.
                }
            }
        }
        return null;
    }
}
