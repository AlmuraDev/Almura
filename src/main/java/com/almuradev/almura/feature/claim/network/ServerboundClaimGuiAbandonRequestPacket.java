/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundClaimGuiAbandonRequestPacket implements Message {

    public double x, y, z;
    public String worldName;

    public ServerboundClaimGuiAbandonRequestPacket() {}

    public ServerboundClaimGuiAbandonRequestPacket(final double x, final double y, final double z, final String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.worldName = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeString(this.worldName);
    }

}
