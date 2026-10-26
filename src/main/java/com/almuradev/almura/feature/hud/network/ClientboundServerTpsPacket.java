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
 * The server's ticks per second, sent to every player once a second for the Origin HUD.
 */
public class ClientboundServerTpsPacket implements Message {

    public double tps;

    public ClientboundServerTpsPacket() {
    }

    public ClientboundServerTpsPacket(final double tps) {
        this.tps = tps;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.tps = buf.readDouble();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeDouble(this.tps);
    }
}
