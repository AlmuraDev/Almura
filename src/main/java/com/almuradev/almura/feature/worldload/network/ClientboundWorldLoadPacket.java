/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * Tells the client the server started or finished loading a world.
 */
public final class ClientboundWorldLoadPacket implements Message {

    public boolean started;
    public String worldName = "";

    public ClientboundWorldLoadPacket() {
    }

    public ClientboundWorldLoadPacket(final boolean started, final String worldName) {
        this.started = started;
        this.worldName = worldName;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.started = buf.readBoolean();
        this.worldName = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.started);
        buf.writeString(this.worldName);
    }
}
