/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundExchangeListItemsResponsePacket implements Message {

    @Nullable public String id;
    @Nullable public List<ListItem> items;

    public ClientboundExchangeListItemsResponsePacket() {

    }

    public ClientboundExchangeListItemsResponsePacket(final String id, final List<ListItem> items) {
        checkNotNull(id);
        checkNotNull(items);

        this.id = id;
        this.items = items;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        final int count = buf.readVarInt();

        if (count > 0) {
            this.items = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final int record = buf.readInteger();
                final ResourceLocation location = SerializationUtil.fromString(buf.readString());
                if (location == null) {
                    // TODO Malformed ResourceLocation
                    continue;
                }

                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    // TODO Unknown item
                    continue;
                }

                final int quantity = buf.readInteger();
                final int metadata = buf.readInteger();

                final Instant created;
                try {
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readVarInt()));
                } catch (IOException | ClassNotFoundException e) {
                    // TODO Bad created date
                    continue;
                }

                final UUID seller = buf.readUniqueId();

                final BigDecimal price = SerializationUtil.fromBytes(buf.readBytes(buf.readVarInt()));
                final int index = buf.readInteger();

                items.add(new BasicListItem(record, created, seller, item, quantity, metadata, price, index));
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeVarInt(this.items == null ? 0 : this.items.size());

        if (this.items != null) {
            for (final ListItem item : this.items) {
                final ResourceLocation location = item.getItem().getRegistryName();
                if (location == null) {
                    // TODO Bad item, no location
                    continue;
                }

                final byte[] createdData;
                try {
                    createdData = SerializationUtil.objectToBytes(item.getCreated());
                } catch (IOException e) {
                    // TODO Malformed created date
                    continue;
                }

                buf.writeInteger(item.getRecord());

                buf.writeString(SerializationUtil.toString(location));

                buf.writeInteger(item.getQuantity());
                buf.writeInteger(item.getMetadata());

                buf.writeVarInt(createdData.length);
                buf.writeBytes(createdData);

                buf.writeUniqueId(item.getSeller());

                final byte[] priceData = SerializationUtil.toBytes(item.getPrice());
                buf.writeVarInt(priceData.length);
                buf.writeBytes(priceData);

                buf.writeInteger(item.getIndex());
            }
        }
    }
}
