/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundPageChangeResponsePacket implements Message {

    public PageChangeType changeType;
    public boolean success;
    public String id;
    public String message;

    public ClientboundPageChangeResponsePacket() {
    }

    public ClientboundPageChangeResponsePacket(PageChangeType changeType, boolean success, String id, String message) {
        this.changeType = changeType;
        this.success = success;
        this.id = id;
        this.message = message;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.changeType = PageChangeType.of(buf.readByte());
        if (this.changeType != null) {
            this.success = buf.readBoolean();
            this.id = buf.readString();
            this.message = buf.readString();
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeByte((byte) this.changeType.ordinal());
        buf.writeBoolean(this.success);
        buf.writeString(this.id);
        buf.writeString(this.message);
    }
}
