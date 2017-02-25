/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

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

public class S03PlayerAccessories implements IMessage, IMessageHandler<S03PlayerAccessories, IMessage> {

    public boolean add;
    public String username;
    public String accessoryIdentifier;
    public String textureName;

    public S03PlayerAccessories() {}

    public S03PlayerAccessories(boolean add, String username, String accessoryIdentifier, String textureName) {
        this.add = add;
        this.username = username;
        this.accessoryIdentifier = accessoryIdentifier;
        this.textureName = textureName;
    }

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
        buf.writeBoolean(add);
        ByteBufUtils.writeUTF8String(buf, username);
        ByteBufUtils.writeUTF8String(buf, accessoryIdentifier);
        if (add) {
            ByteBufUtils.writeUTF8String(buf, textureName);
        }
    }

    @Override
    public IMessage onMessage(S03PlayerAccessories message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            final EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(message.username);
            if (player != null) {
                if (message.add) {
                    AccessoryManager.addAccessory(player, new ResourceLocation(Almura.MOD_ID, "accessory_" + message.textureName),
                            message.accessoryIdentifier);
                } else {
                    AccessoryManager.removeAccessory(player, message.accessoryIdentifier);
                }
            }
        }
        return null;
    }
}
