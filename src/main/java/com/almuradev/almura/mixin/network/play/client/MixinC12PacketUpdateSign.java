/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(C12PacketUpdateSign.class)
public abstract class MixinC12PacketUpdateSign extends Packet {

    @Shadow
    private int x;

    @Shadow
    private int y;

    @Shadow
    private int z;

    @Shadow
    private String[] lines;

    @Overwrite
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException {
        this.x = p_148837_1_.readInt();
        this.y = p_148837_1_.readShort();
        this.z = p_148837_1_.readInt();
        this.lines = new String[4];

        for (int i = 0; i < 4; ++i) {
            // Almura Start - 15 -> 30
            this.lines[i] = p_148837_1_.readStringFromBuffer(30);
        }
    }
}
