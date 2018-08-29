/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
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
    @Nullable public List<ForSaleItem> listedItems;
    @Nullable public List<ListItem> lastKnownPriceItems;
    @Nullable public List<ListedItemUpdate> serverListedItems;
    @Nullable public List<LastKnownPriceUpdate> serverLastKnownPriceItems;

    public ClientboundListItemsSaleStatusPacket() {
    }

    public ClientboundListItemsSaleStatusPacket(final String id, @Nullable final List<ForSaleItem> listedItems, @Nullable final List<ListItem>
      lastKnownPriceItems) {
        checkNotNull(id);

        this.id = id;
        this.listedItems = listedItems;
        this.lastKnownPriceItems = lastKnownPriceItems;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        int count = buf.readInteger();

        if (count > 0) {
            this.serverListedItems = new ArrayList<>();

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
                final BigDecimal price = ByteBufUtil.readBigDecimal((ByteBuf) buf);

                this.serverListedItems.add(new ListedItemUpdate(listItemRecNo, forSaleItemRecNo, created, price));
            }
        }

        count = buf.readInteger();
        if (count > 0) {
            this.serverLastKnownPriceItems = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final int listItemRecNo = buf.readInteger();
                final BigDecimal lastKnownPrice = ByteBufUtil.readBigDecimal((ByteBuf) buf);

                this.serverLastKnownPriceItems.add(new LastKnownPriceUpdate(listItemRecNo, lastKnownPrice));
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeInteger(this.listedItems == null ? 0 : this.listedItems.size());

        if (this.listedItems != null) {
            for (final ForSaleItem listedItem : this.listedItems) {
                buf.writeInteger(listedItem.getListItem().getRecord());
                buf.writeInteger(listedItem.getRecord());
                try {
                    final byte[] createdData = SerializationUtil.objectToBytes(listedItem.getCreated());
                    buf.writeInteger(createdData.length);
                    buf.writeBytes(createdData);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                ByteBufUtil.writeBigDecimal((ByteBuf) buf, listedItem.getPrice());
            }
        }

        buf.writeInteger(this.lastKnownPriceItems == null ? 0 : this.lastKnownPriceItems.size());
        if (this.lastKnownPriceItems != null) {
            for (final ListItem delistedItem : this.lastKnownPriceItems) {
                delistedItem.getLastKnownPrice().ifPresent(lastKnownPrice -> {
                    buf.writeInteger(delistedItem.getRecord());
                    ByteBufUtil.writeBigDecimal((ByteBuf) buf, lastKnownPrice);
                });
            }
        }
    }

    public static class ListedItemUpdate {
        public final int listItemRecNo;
        public final int forSaleItemRecNo;
        public final Instant created;
        public final BigDecimal price;

        ListedItemUpdate(final int listItemRecNo, final int forSaleItemRecNo, final Instant created, final BigDecimal price) {
            this.listItemRecNo = listItemRecNo;
            this.forSaleItemRecNo = forSaleItemRecNo;
            this.created = created;
            this.price = price;
        }
    }

    public static class LastKnownPriceUpdate {
        public final int listItemRecNo;
        public final BigDecimal lastKnownPrice;

        public LastKnownPriceUpdate(final int listItemRecNo, final BigDecimal lastKnownPrice) {
            this.listItemRecNo = listItemRecNo;
            this.lastKnownPrice = lastKnownPrice;
        }
    }
}
