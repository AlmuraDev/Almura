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

public final class ServerboundForSaleFilterResponsePacket implements Message {

    @Nullable public String id;
    @Nullable public String filter;
    @Nullable public String sort;
    public int skip;
    public int limit;

    public ServerboundForSaleFilterResponsePacket() {
    }

    public ServerboundForSaleFilterResponsePacket(@Nullable final String id, @Nullable final String filter, @Nullable final String sort,
        final int skip, final int limit) {
        this.id = id;
        this.filter = filter;
        this.sort = sort;
        this.skip = skip;
        this.limit = limit;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();

        if (buf.readBoolean()) {
            this.filter = buf.readString();
        }

        if (buf.readBoolean()) {
            this.sort = buf.readString();
        }

        this.skip = buf.readInteger();
        this.limit = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeBoolean(this.filter != null);

        if (this.filter != null) {
            buf.writeString(this.filter);
        }

        buf.writeBoolean(this.sort != null);

        if (this.sort != null) {
            buf.writeString(this.sort);
        }

        buf.writeInteger(this.skip);
        buf.writeInteger(this.limit);
    }
}
