/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameBlockInformation;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class S01OpenBlockInformationGui implements IMessage, IMessageHandler<S01OpenBlockInformationGui, IMessage> {

    public Block block;
    public int metadata;
    public float hardness;

    public S01OpenBlockInformationGui() {
    }

    public S01OpenBlockInformationGui(Block block, int metadata, float hardness) {
        this.block = block;
        this.metadata = metadata;
        this.hardness = hardness;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        block = Block.getBlockById(buf.readInt());
        metadata = buf.readInt();
        hardness = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Block.getIdFromBlock(block));
        buf.writeInt(metadata);
        buf.writeFloat(hardness);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S01OpenBlockInformationGui message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().displayGuiScreen(new IngameBlockInformation(null, message.block, message.metadata, message.hardness));
        }
        return null;
    }
}
