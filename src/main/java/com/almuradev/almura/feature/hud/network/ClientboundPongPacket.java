/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * The server's answer to a {@link ServerboundPingPacket}, carrying back the client's send time.
 */
public class ClientboundPongPacket implements Message {

    public long time;

    public ClientboundPongPacket() {
    }

    public ClientboundPongPacket(final long time) {
        this.time = time;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.time = buf.readLong();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeLong(this.time);
    }
}
