/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
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
                final ResourceLocation location = new ResourceLocation(buf.readString(), buf.readString());
                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    // TODO
                    continue;
                }

                final int quantity = buf.readInteger();
                final int metadata = buf.readInteger();

                final Instant created;
                try {
                    created = PacketUtil.bytesToObject(buf.readBytes(buf.readVarInt()));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                final UUID seller = buf.readUniqueId();

                final BigDecimal price = PacketUtil.fromBytes(buf.readBytes(buf.readVarInt()));
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
            this.items.forEach(item -> {
                buf.writeInteger(item.getRecord());
                
                final ResourceLocation location = item.getItem().getRegistryName();

                checkNotNull(location);

                buf.writeString(location.getResourceDomain());
                buf.writeString(location.getResourcePath());

                buf.writeInteger(item.getQuantity());
                buf.writeInteger(item.getMetadata());

                final byte[] createdData;
                try {
                    createdData = PacketUtil.objectToBytes(item.getCreated());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buf.writeVarInt(createdData.length);
                buf.writeBytes(createdData);

                buf.writeUniqueId(item.getSeller());

                final byte[] priceData = PacketUtil.toBytes(item.getPrice());
                buf.writeVarInt(priceData.length);
                buf.writeBytes(priceData);

                buf.writeInteger(item.getIndex());

            });
        }
    }
}
