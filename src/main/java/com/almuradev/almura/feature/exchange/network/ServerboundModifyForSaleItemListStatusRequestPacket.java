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
import com.almuradev.almura.shared.util.SerializationUtil;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public final class ServerboundModifyForSaleItemListStatusRequestPacket implements Message {

    @Nullable public ListStatusType type;
    @Nullable public String id;
    public int listItemRecNo;
    @Nullable public BigDecimal price;

    public ServerboundModifyForSaleItemListStatusRequestPacket() {
    }

    public ServerboundModifyForSaleItemListStatusRequestPacket(final ListStatusType type, final String id, final int listItemRecNo,
        @Nullable final BigDecimal price) {
        checkNotNull(type);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);

        this.id = id;
        this.listItemRecNo = listItemRecNo;
        this.type = type;

        if (this.type != ListStatusType.DE_LIST) {
            checkNotNull(price);
            checkState(price.doubleValue() >= 0);
            this.price = price;
        }
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

        if (this.type != ListStatusType.DE_LIST) {
            checkNotNull(this.price);
            checkState(this.price.doubleValue() >= 0);

            final byte[] priceData = SerializationUtil.toBytes(this.price);
            buf.writeInteger(priceData.length);
            buf.writeBytes(priceData);
        }
    }
}
