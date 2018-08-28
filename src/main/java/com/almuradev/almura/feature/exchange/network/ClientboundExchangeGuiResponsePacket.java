/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ClientboundExchangeGuiResponsePacket implements Message {

    @Nullable public String id;

    public ClientboundExchangeGuiResponsePacket() {
    }

    public ClientboundExchangeGuiResponsePacket(final String id) {
        checkNotNull(id);
        this.id = id;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
    }
}
