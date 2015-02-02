/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(value = C01PacketChatMessage.class)
public abstract class MixinC01PacketChatMessage extends Packet {

    @Shadow
    private String field_149440_a;

    @Overwrite
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException {
        this.field_149440_a = p_148837_1_.readStringFromBuffer(1000);
    }
}
