/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameFarmInformation;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class S04OpenFarmInformationGui implements IMessage, IMessageHandler<S04OpenFarmInformationGui, IMessage> {

    public Block block;
    public int metadata;
    public boolean fertile;
    public float temp;
    public float rain;

    public S04OpenFarmInformationGui() {
    }

    public S04OpenFarmInformationGui(Block block, int metadata, boolean fertile, float temp, float rain) {
        this.block = block;
        this.metadata = metadata;
        this.fertile = fertile;
        this.temp = temp;
        this.rain = rain;
        
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        block = Block.getBlockById(buf.readInt());
        metadata = buf.readInt();
        fertile = buf.readBoolean();
        temp = buf.readFloat();
        rain = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Block.getIdFromBlock(block));
        buf.writeInt(metadata);
        buf.writeBoolean(fertile);
        buf.writeFloat(temp);
        buf.writeFloat(rain);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S04OpenFarmInformationGui message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().displayGuiScreen(new IngameFarmInformation(message.block, message.metadata, message.fertile, message.temp, message.rain));
        }
        return null;
    }  
}
