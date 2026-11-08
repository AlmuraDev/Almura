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
 * Sent by the client once a second to measure its ping; the server sends the time straight back in a {@link ClientboundPongPacket}.
 */
public class ServerboundPingPacket implements Message {

    // The client's System.nanoTime() when it sent this
    public long time;

    public ServerboundPingPacket() {
    }

    public ServerboundPingPacket(final long time) {
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
