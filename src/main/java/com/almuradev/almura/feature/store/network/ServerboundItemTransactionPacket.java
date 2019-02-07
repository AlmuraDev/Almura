/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.store.StoreItemSegmentType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.Locale;

import javax.annotation.Nullable;

public final class ServerboundItemTransactionPacket implements Message {

    @Nullable public String id;
    @Nullable public StoreItemSegmentType type;
    @Nullable public int recNo;
    @Nullable public int quantity;

    public ServerboundItemTransactionPacket() {
    }

    public ServerboundItemTransactionPacket(final String id, final StoreItemSegmentType type, final int recNo, final int quantity) {
        checkNotNull(id);
        checkNotNull(type);
        checkState(recNo >= 0);
        checkState(quantity >= 0);

        this.id = id;
        this.type = type;
        this.recNo = recNo;
        this.quantity = quantity;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.type = StoreItemSegmentType.valueOf(buf.readString());
        this.recNo = buf.readInteger();
        this.quantity = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.type);
        checkState(this.recNo >= 0);
        checkState(this.quantity >= 0);

        buf.writeString(this.id);
        buf.writeString(this.type.name().toUpperCase(Locale.ENGLISH));
        buf.writeInteger(this.recNo);
        buf.writeInteger(this.quantity);
    }
}
