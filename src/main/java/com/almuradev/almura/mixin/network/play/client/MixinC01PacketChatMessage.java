/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.network.play.client;

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
    private String message;

    @Overwrite
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException {
        this.message = p_148837_1_.readStringFromBuffer(1000);
    }
}
