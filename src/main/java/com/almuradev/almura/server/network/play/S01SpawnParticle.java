/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class S01SpawnParticle implements IMessage, IMessageHandler<S01SpawnParticle, IMessage> {

    public String particleName;
    public int x, y, z;
    public double offsetX, offsetY, offsetZ;

    public S01SpawnParticle() {
    }

    public S01SpawnParticle(String particleName, int x, int y, int z, double offsetX, double offsetY, double offsetZ) {
        this.particleName = particleName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        particleName = ByteBufUtils.readUTF8String(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        offsetX = buf.readDouble();
        offsetY = buf.readDouble();
        offsetZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, particleName);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeDouble(offsetX);
        buf.writeDouble(offsetY);
        buf.writeDouble(offsetZ);
    }

    @Override
    public IMessage onMessage(S01SpawnParticle message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().theWorld
                    .spawnParticle(message.particleName, message.x, message.y, message.z, message.offsetX, message.offsetY, message.offsetZ);
        }
        return null;
    }
}
