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

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundTransactionRequestPacket implements Message {

    @Nullable public String id;
    public int listItemRecNo;
    public int quantity;

    public ServerboundTransactionRequestPacket() {
    }

    public ServerboundTransactionRequestPacket(final String id, final int listItemRecNo, final int quantity) {
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkState(quantity >= 0);

        this.id = id;
        this.listItemRecNo = listItemRecNo;
        this.quantity = quantity;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.listItemRecNo = buf.readInteger();
        this.quantity = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkState(this.listItemRecNo >= 0);
        checkState(this.quantity >= 0);

        buf.writeString(this.id);
        buf.writeInteger(this.listItemRecNo);
        buf.writeInteger(this.quantity);
    }
}
