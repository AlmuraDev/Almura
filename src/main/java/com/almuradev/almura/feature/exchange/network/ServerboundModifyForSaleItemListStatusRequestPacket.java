/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.shared.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

        if (this.type == ListStatusType.LIST || this.type == ListStatusType.ADJUST_PRICE) {
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

        if (this.type == ListStatusType.LIST || this.type == ListStatusType.ADJUST_PRICE) {
            this.price = ByteBufUtil.readBigDecimal((ByteBuf) buf);
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);
        checkNotNull(this.id);
        checkState(this.listItemRecNo >= 0);

        buf.writeString(this.type.name().toUpperCase());
        buf.writeString(this.id);
        buf.writeInteger(this.listItemRecNo);

        if (this.type == ListStatusType.LIST || this.type == ListStatusType.ADJUST_PRICE) {
            checkNotNull(this.price);
            checkState(this.price.doubleValue() >= 0);

            ByteBufUtil.writeBigDecimal((ByteBuf) buf, this.price);
        }
    }
}
