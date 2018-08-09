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

import com.almuradev.almura.feature.exchange.ListStatusType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundModifyForSaleItemListStatusRequestPacket implements Message {

    @Nullable public ListStatusType type;
    @Nullable public String id;
    public int listItemRecNo;

    public ServerboundModifyForSaleItemListStatusRequestPacket() {
    }

    public ServerboundModifyForSaleItemListStatusRequestPacket(final ListStatusType type, final String id, final int listItemRecNo) {
        checkNotNull(type);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);

        this.id = id;
        this.listItemRecNo = listItemRecNo;
        this.type = type;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = ListStatusType.valueOf(buf.readString());
        this.id = buf.readString();
        this.listItemRecNo = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);
        checkNotNull(this.id);
        checkState(this.listItemRecNo >= 0);

        buf.writeString(this.type.name().toLowerCase());
        buf.writeString(this.id);
        buf.writeInteger(this.listItemRecNo);
    }
}
