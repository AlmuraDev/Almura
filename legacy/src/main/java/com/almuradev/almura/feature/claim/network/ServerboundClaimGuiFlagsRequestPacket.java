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

/**
 * One checkbox changed in the claim manager's Flags section. Only the box that was clicked is sent, so a box showing a stale state can't
 * change anything else.
 */
public final class ServerboundClaimGuiFlagsRequestPacket implements Message {

    // Names "Kill monsters in claim", which isn't a ClaimFlag.
    public static final String KILL_MONSTERS = "KILL_MONSTERS";

    public double x, y, z;
    public String worldName;
    // A ClaimFlag name, or KILL_MONSTERS.
    public String option;
    public boolean value;

    public ServerboundClaimGuiFlagsRequestPacket() {}

    public ServerboundClaimGuiFlagsRequestPacket(final double x, final double y, final double z, final String worldName, final String option,
        final boolean value) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.option = option;
        this.value = value;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.worldName = buf.readString();
        this.option = buf.readString();
        this.value = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeString(this.worldName);
        buf.writeString(this.option);
        buf.writeBoolean(this.value);
    }
}
