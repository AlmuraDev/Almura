/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.Session;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityClientPlayerMP.class)
public abstract class MixinEntityClientPlayerMP extends EntityPlayerSP {

    @Shadow
    public NetHandlerPlayClient sendQueue;
    
    public MixinEntityClientPlayerMP(Minecraft p_i1238_1_, World p_i1238_2_, Session p_i1238_3_, int p_i1238_4_) {
        super(p_i1238_1_, p_i1238_2_, p_i1238_3_, p_i1238_4_);    
    }
    
    @Overwrite
    public void sendChatMessage(String p_71165_1_)
    {
        final C01PacketChatMessage message = new C01PacketChatMessage();
        if (p_71165_1_.length() > 1000)
        {
            p_71165_1_ = p_71165_1_.substring(0, 1000);
        }
        message.field_149440_a = p_71165_1_;
        this.sendQueue.addToSendQueue(message);
    }
}
