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

import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.util.SerializationUtil;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class ClientboundListItemsSaleStatusPacket implements Message {

    @Nullable public String id;
    @Nullable public List<ForSaleItem> toClientItems;
    @Nullable public List<ForSaleItemCandidate> fromServerItems;

    public ClientboundListItemsSaleStatusPacket() {
    }

    public ClientboundListItemsSaleStatusPacket(final String id, final List<ForSaleItem> toClientItems) {
        checkNotNull(id);
        checkNotNull(toClientItems);
        checkState(!toClientItems.isEmpty());

        this.id = id;
        this.toClientItems = toClientItems;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();

        checkState(count > 0);

        this.fromServerItems = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int listItemRecNo = buf.readInteger();
            final int remainingQuantity = buf.readInteger();
            final BigDecimal price = SerializationUtil.fromBytes(buf.readBytes(buf.readInteger()));

            this.fromServerItems.add(new ForSaleItemCandidate(listItemRecNo, remainingQuantity, price));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.toClientItems);
        checkState(!this.toClientItems.isEmpty());

        buf.writeInteger(this.toClientItems.size());

        this.toClientItems.forEach(item -> {
            buf.writeInteger(item.getListItem().getRecord());
            buf.writeInteger(item.getQuantity());
            final byte[] priceData = SerializationUtil.toBytes(item.getPrice());
            buf.writeInteger(priceData.length);
            buf.writeBytes(priceData);
        });
    }

    public static class ForSaleItemCandidate {
        public final int listItemRecNo;
        public final int quantityRemaining;
        public final BigDecimal price;

        ForSaleItemCandidate(final int listItemRecNo, final int quantityRemaining, final BigDecimal price) {
            this.listItemRecNo = listItemRecNo;
            this.quantityRemaining = quantityRemaining;
            this.price = price;
        }
    }
}
