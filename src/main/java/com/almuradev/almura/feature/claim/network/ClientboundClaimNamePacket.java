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

public final class ClientboundClaimNamePacket implements Message {

    public String name;

    public ClientboundClaimNamePacket() {
    }

    public ClientboundClaimNamePacket(final String name) {
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
