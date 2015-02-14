/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.renderer.accessories.AccessoryManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class B04PlayerAccessories implements IMessage, IMessageHandler<B04PlayerAccessories, IMessage> {
    public boolean add;
    public String username;
    public String accessoryIdentifier;
    public String textureName;

    @Override
    public void fromBytes(ByteBuf buf) {
        add = buf.readBoolean();
        username = ByteBufUtils.readUTF8String(buf);
        accessoryIdentifier = ByteBufUtils.readUTF8String(buf);
        if (add) {
            textureName = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public IMessage onMessage(B04PlayerAccessories message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            final EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(message.username);
            if (player != null) {
                if (message.add) {
                    AccessoryManager.addAccessory(player, new ResourceLocation(Almura.MOD_ID, "accessory_" + message.textureName), message.accessoryIdentifier);
                } else {
                    AccessoryManager.removeAccessory(player, message.accessoryIdentifier);
                }
            }
        }
        return null;
    }
}
