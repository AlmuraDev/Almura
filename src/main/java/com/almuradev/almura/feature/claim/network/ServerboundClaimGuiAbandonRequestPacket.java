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

    public String worldName;
    public double x, y, z;

    public ServerboundClaimGuiAbandonRequestPacket() {
    }

    public ServerboundClaimGuiAbandonRequestPacket(final String worldName, final double x, final double y, final double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.worldName = buf.readString();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.worldName);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

}
