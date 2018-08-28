/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.exchange.ExchangeConstants;
import com.almuradev.almura.feature.exchange.ExchangeGuiType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ClientboundExchangeGuiResponsePacket implements Message {

    @Nullable public ExchangeGuiType type;
    @Nullable public String id;
    public int limit;

    public ClientboundExchangeGuiResponsePacket() {

    }

    public ClientboundExchangeGuiResponsePacket(final ExchangeGuiType type) {
        this(type, null);
    }

    public ClientboundExchangeGuiResponsePacket(final ExchangeGuiType type, @Nullable final String id) {
        this(type, id, -2); // This constructor should never be called for SPECIFIC_OFFER so pass -2 to intentionally fail
    }

    public ClientboundExchangeGuiResponsePacket(final ExchangeGuiType type, @Nullable final String id, final int limit) {
        checkNotNull(type);

        this.type = type;

        if (this.type != ExchangeGuiType.MANAGE) {
            checkNotNull(id);
            this.id = id;
        }

        if (this.type == ExchangeGuiType.SPECIFIC) {
            checkState(limit >= ExchangeConstants.UNLIMITED);
            this.limit = limit;
        }
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = ExchangeGuiType.valueOf(buf.readString());

        if (this.type != ExchangeGuiType.MANAGE) {
            this.id = buf.readString();
        }

        if (this.type == ExchangeGuiType.SPECIFIC) {
            this.limit = buf.readInteger();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);

        buf.writeString(this.type.name().toUpperCase());

        if (this.type != ExchangeGuiType.MANAGE) {
            checkNotNull(this.id);

            buf.writeString(this.id);
        }

        if (this.type == ExchangeGuiType.SPECIFIC) {
            buf.writeInteger(this.limit);
        }
    }
}
