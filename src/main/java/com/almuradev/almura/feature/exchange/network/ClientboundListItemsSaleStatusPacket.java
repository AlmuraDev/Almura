/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.util.ByteBufUtil;
import com.almuradev.almura.shared.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class ClientboundListItemsSaleStatusPacket implements Message {

    @Nullable public String id;
    @Nullable public List<ForSaleItem> toClientItems;
    @Nullable public List<ForSaleItemCandidate> fromServerItems;

    public ClientboundListItemsSaleStatusPacket() {
    }

    public ClientboundListItemsSaleStatusPacket(final String id, @Nullable final List<ForSaleItem> toClientItems) {
        checkNotNull(id);

        this.id = id;
        this.toClientItems = toClientItems;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        final int count = buf.readInteger();

        if (count > 0) {
            this.fromServerItems = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final int listItemRecNo = buf.readInteger();
                final int forSaleItemRecNo = buf.readInteger();
                final Instant created;
                try {
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                final int remainingQuantity = buf.readInteger();
                final BigDecimal price = ByteBufUtil.readBigDecimal((ByteBuf) buf);

                this.fromServerItems.add(new ForSaleItemCandidate(listItemRecNo, forSaleItemRecNo, created, remainingQuantity, price));
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeInteger(this.toClientItems == null ? 0 : this.toClientItems.size());

        if (this.toClientItems != null) {
            for (final ForSaleItem item : this.toClientItems) {
                buf.writeInteger(item.getListItem().getRecord());
                buf.writeInteger(item.getRecord());
                try {
                    final byte[] createdData = SerializationUtil.objectToBytes(item.getCreated());
                    buf.writeInteger(createdData.length);
                    buf.writeBytes(createdData);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                buf.writeInteger(item.getQuantity());
                ByteBufUtil.writeBigDecimal((ByteBuf) buf, item.getPrice());
            }
        }
    }

    public static class ForSaleItemCandidate {
        public final int listItemRecNo;
        public final int forSaleItemRecNo;
        public final Instant created;
        public final int quantityRemaining;
        public final BigDecimal price;

        ForSaleItemCandidate(final int listItemRecNo, final int forSaleItemRecNo, final Instant created, final int quantityRemaining,
            final BigDecimal price) {
            this.listItemRecNo = listItemRecNo;
            this.forSaleItemRecNo = forSaleItemRecNo;
            this.created = created;
            this.quantityRemaining = quantityRemaining;
            this.price = price;
        }
    }
}
