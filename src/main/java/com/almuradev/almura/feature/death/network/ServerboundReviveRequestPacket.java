/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServerboundReviveRequestPacket implements Message {

    public int dimID;
    public double x, y, z;

    public ServerboundReviveRequestPacket(){}

    public ServerboundReviveRequestPacket(int dimID, double x, double y, double z) {
        this.dimID = dimID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.dimID = buf.readInteger();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.dimID);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }
}
