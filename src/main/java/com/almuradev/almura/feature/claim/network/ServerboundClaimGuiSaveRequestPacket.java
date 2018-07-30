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

public final class ServerboundClaimGuiSaveRequestPacket implements Message {

    public String worldName, claimName, claimGreeting, claimFarewell;
    public double x, y, z;

    public ServerboundClaimGuiSaveRequestPacket() {
    }

    public ServerboundClaimGuiSaveRequestPacket(final String worldName, final double x, final double y, final double z, final String claimName,
        final String claimGreeting, final String claimFarewell) {
        this.claimName = claimName;
        this.claimGreeting = claimGreeting;
        this.claimFarewell = claimFarewell;
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
        this.claimName = buf.readString();
        this.claimGreeting = buf.readString();
        this.claimFarewell = buf.readString();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.worldName);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeString(this.claimName);
        buf.writeString(this.claimGreeting);
        buf.writeString(this.claimFarewell);
    }

}
