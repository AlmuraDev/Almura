/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class S00AdditionalWorldInfo implements IMessage, IMessageHandler<S00AdditionalWorldInfo, IMessage> {
    public String worldName;

    public S00AdditionalWorldInfo() {
    }

    public S00AdditionalWorldInfo(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        worldName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, worldName);
    }

    @Override
    public IMessage onMessage(S00AdditionalWorldInfo message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            IngameHUD.INSTANCE.worldDisplay.setText(message.worldName);
        }
        return null;
    }
}
