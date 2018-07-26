/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.ExchangeModifyType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundModifyExchangeRequestPacket implements Message {

    @Nullable public ExchangeModifyType type;
    @Nullable public String id, name, permission;
    public boolean isHidden;

    public ServerboundModifyExchangeRequestPacket() {
    }

    public ServerboundModifyExchangeRequestPacket(final ExchangeModifyType type, final String id, @Nullable final String name, @Nullable final String
        permission, final boolean isHidden) {
        checkNotNull(type);
        checkNotNull(id);

        this.type = type;
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.isHidden = isHidden;
    }

    public ServerboundModifyExchangeRequestPacket(final String id) {
        this(ExchangeModifyType.DELETE, id, null, null, false);
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = ExchangeModifyType.valueOf(buf.readString());
        this.id = buf.readString();

        if (this.type != ExchangeModifyType.DELETE) {
            this.name = buf.readString();
            this.permission = buf.readString();
            this.isHidden = buf.readBoolean();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);
        checkNotNull(this.id);

        if (this.type != ExchangeModifyType.DELETE) {
            checkNotNull(this.name);
            checkNotNull(this.permission);
        }

        buf.writeString(this.type.name().toUpperCase());
        buf.writeString(this.id);

        if (this.type != ExchangeModifyType.DELETE) {
            buf.writeString(this.name);
            buf.writeString(this.permission);
            buf.writeBoolean(this.isHidden);
        }
    }
}
