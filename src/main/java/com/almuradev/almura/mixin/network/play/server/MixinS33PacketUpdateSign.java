/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(S33PacketUpdateSign.class)
public abstract class MixinS33PacketUpdateSign extends Packet {

    @Shadow
    private int field_149352_a;
    @Shadow
    private int field_149350_b;
    @Shadow
    private int field_149351_c;
    @Shadow
    private String[] field_149349_d;

    @Overwrite
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException {
        this.field_149352_a = p_148837_1_.readInt();
        this.field_149350_b = p_148837_1_.readShort();
        this.field_149351_c = p_148837_1_.readInt();
        this.field_149349_d = new String[4];

        for (int i = 0; i < 4; ++i) {
            this.field_149349_d[i] = p_148837_1_.readStringFromBuffer(30);
        }
    }
}
