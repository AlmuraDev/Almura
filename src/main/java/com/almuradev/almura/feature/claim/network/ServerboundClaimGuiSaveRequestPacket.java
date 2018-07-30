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

    public String claimName, claimGreeting, claimFarewell;
    public double x, y, z;
    public String worldName;

    public ServerboundClaimGuiSaveRequestPacket() {}

    public ServerboundClaimGuiSaveRequestPacket(final String claimName, final String claimGreeting, final String claimFarewell, final double x, final double y, final double z, final String worldName) {
        this.claimName = claimName;
        this.claimGreeting = claimGreeting;
        this.claimFarewell = claimFarewell;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.claimName = buf.readString();
        this.claimGreeting = buf.readString();
        this.claimFarewell = buf.readString();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.worldName = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.claimName);
        buf.writeString(this.claimGreeting);
        buf.writeString(this.claimFarewell);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeString(this.worldName);
    }

}
