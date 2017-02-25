/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameBlockWireframe;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public class S02OpenBlockWireframeGui implements IMessage, IMessageHandler<S02OpenBlockWireframeGui, IMessage> {

    public AxisAlignedBB wireframe;
    public int x, y, z;
    public ItemStack stack;

    public S02OpenBlockWireframeGui() {
    }

    public S02OpenBlockWireframeGui(ItemStack stack, int x, int y, int z, AxisAlignedBB wireframe) {
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
        this.wireframe = wireframe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        wireframe =
                AxisAlignedBB
                        .getBoundingBox(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeDouble(wireframe.minX);
        buf.writeDouble(wireframe.minY);
        buf.writeDouble(wireframe.minZ);
        buf.writeDouble(wireframe.maxX);
        buf.writeDouble(wireframe.maxY);
        buf.writeDouble(wireframe.maxZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S02OpenBlockWireframeGui message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().displayGuiScreen(
                    new IngameBlockWireframe(Minecraft.getMinecraft().theWorld,
                            Minecraft.getMinecraft().theWorld.getBlock(message.x, message.y, message.z),
                            message.x, message.y, message.z, wireframe));
        }
        return null;
    }
}
