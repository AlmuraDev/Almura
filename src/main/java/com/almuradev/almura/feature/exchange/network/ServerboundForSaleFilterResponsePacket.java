/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundForSaleFilterResponsePacket implements Message {

    @Nullable public String filter;

    public ServerboundForSaleFilterResponsePacket() {
    }

    public ServerboundForSaleFilterResponsePacket(@Nullable final String filter) {
        this.filter = filter;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        if (buf.readBoolean()) {
            this.filter = buf.readString();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.filter != null);

        if (this.filter != null) {
            buf.writeString(this.filter);
        }
    }
}
