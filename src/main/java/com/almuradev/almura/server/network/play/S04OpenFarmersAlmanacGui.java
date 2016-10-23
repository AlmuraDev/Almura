/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameFarmersAlmanac;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class S04OpenFarmersAlmanacGui implements IMessage, IMessageHandler<S04OpenFarmersAlmanacGui, IMessage> {

    public Block block;
    public int metadata;
    public boolean fertile;
    public float temp;
    public float rain;
    public int areaBlockLight;
    public int sunlight;

    public S04OpenFarmersAlmanacGui() {
    }

    public S04OpenFarmersAlmanacGui(Block block, int metadata, boolean fertile, float temp, float rain, int areaBlockLight, int sunlight) {
        this.block = block;
        this.metadata = metadata;
        this.fertile = fertile;
        this.temp = temp;
        this.rain = rain;
        this.areaBlockLight = areaBlockLight;
        this.sunlight = sunlight;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        block = Block.getBlockById(buf.readInt());
        metadata = buf.readInt();
        fertile = buf.readBoolean();
        temp = buf.readFloat();
        rain = buf.readFloat();
        areaBlockLight = buf.readInt();
        sunlight = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Block.getIdFromBlock(block));
        buf.writeInt(metadata);
        buf.writeBoolean(fertile);
        buf.writeFloat(temp);
        buf.writeFloat(rain);
        buf.writeInt(areaBlockLight);
        buf.writeInt(sunlight);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S04OpenFarmersAlmanacGui message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().displayGuiScreen(new IngameFarmersAlmanac(message.block, message.metadata, message.fertile, message.temp, message.rain, message.areaBlockLight, message.sunlight));
        }
        return null;
    }  
}
