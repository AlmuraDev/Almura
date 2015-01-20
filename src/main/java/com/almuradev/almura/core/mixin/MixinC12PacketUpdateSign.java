/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

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
    private int field_149593_a;

    @Shadow
    private int field_149591_b;

    @Shadow
    private int field_149592_c;

    @Shadow
    private String[] field_149590_d;

    @Overwrite
    public void readPacketData(PacketBuffer p_148837_1_) throws IOException
    {
        this.field_149593_a = p_148837_1_.readInt();
        this.field_149591_b = p_148837_1_.readShort();
        this.field_149592_c = p_148837_1_.readInt();
        this.field_149590_d = new String[4];

        for (int i = 0; i < 4; ++i)
        {
            // Almura Start - 15 -> 30
            this.field_149590_d[i] = p_148837_1_.readStringFromBuffer(30);
        }
    }
}
