/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.ExchangeGuiType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ClientboundExchangeGuiResponsePacket implements Message {

    @Nullable public ExchangeGuiType type;
    @Nullable public String id;

    public ClientboundExchangeGuiResponsePacket() {
    }

    public ClientboundExchangeGuiResponsePacket(final ExchangeGuiType type, @Nullable final String id) {
        checkNotNull(type);

        this.type = type;

        if (this.type != ExchangeGuiType.MANAGE) {
            checkNotNull(id);
            this.id = id;
        }
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = ExchangeGuiType.valueOf(buf.readString());

        if (this.type != ExchangeGuiType.MANAGE) {
            this.id = buf.readString();
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
    }
}
