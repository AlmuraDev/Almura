/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundWorldNamePacket implements Message {

    public String name;

    public ClientboundWorldNamePacket() {
    }

    public ClientboundWorldNamePacket(final String name) {
        this.name = name;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.name = buf.readString();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.name);
    }
}
