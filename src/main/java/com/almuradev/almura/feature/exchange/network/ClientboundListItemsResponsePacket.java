/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.util.SerializationUtil;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundListItemsResponsePacket implements Message {

    @Nullable public String id;
    @Nullable public List<ListItem> listItems;

    public ClientboundListItemsResponsePacket() {

    }

    public ClientboundListItemsResponsePacket(final String id, @Nullable final List<ListItem> listItems) {
        checkNotNull(id);
        checkNotNull(listItems);

        this.id = id;
        this.listItems = listItems;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        final int count = buf.readInteger();

        if (count > 0) {
            this.listItems = new ArrayList<>();
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
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    // TODO Bad created date
                    continue;
                }

                final UUID seller = buf.readUniqueId();
                final String sellerName = buf.readBoolean() ? buf.readString() : null;

                final BigDecimal price = SerializationUtil.fromBytes(buf.readBytes(buf.readInteger()));
                final int index = buf.readInteger();

                final int compoundDataLength = buf.readInteger();
                NBTTagCompound compound = null;

                if (compoundDataLength > 0) {
                    try {
                        compound = SerializationUtil.compoundFromBytes(buf.readBytes(compoundDataLength));
                    } catch (IOException e) {
                        // TODO Malformed tag compound
                        e.printStackTrace();
                        continue;
                    }
                }

                final BasicListItem basicListItem = new BasicListItem(record, created, seller, item, quantity, metadata, price, index, compound);

                if (Sponge.getPlatform().getExecutionType().isClient()) {
                    basicListItem.setSellerName(sellerName);
                }

                this.listItems.add(basicListItem);
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeInteger(this.listItems == null ? 0 : this.listItems.size());

        if (this.listItems != null) {
            for (final ListItem item : this.listItems) {
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

                final NBTTagCompound compound = item.getCompound();
                byte[] compoundData = null;

                if (compound != null) {
                    try {
                        compoundData = SerializationUtil.toBytes(compound);
                    } catch (IOException e) {
                        // TODO Malformed tag compound
                        e.printStackTrace();
                        continue;
                    }
                }

                buf.writeInteger(item.getRecord());

                buf.writeString(SerializationUtil.toString(location));

                buf.writeInteger(item.getQuantity());
                buf.writeInteger(item.getMetadata());

                buf.writeInteger(createdData.length);
                buf.writeBytes(createdData);

                buf.writeUniqueId(item.getSeller());

                final String sellerName = item.getSellerName().orElse(null);
                buf.writeBoolean(sellerName != null);

                if (sellerName != null) {
                    buf.writeString(sellerName);
                }

                final byte[] priceData = SerializationUtil.toBytes(item.getPrice());
                buf.writeInteger(priceData.length);
                buf.writeBytes(priceData);

                buf.writeInteger(item.getIndex());

                if (compoundData == null) {
                    buf.writeInteger(0);
                } else {
                    buf.writeInteger(compoundData.length);
                    buf.writeBytes(compoundData);
                }
            }
        }
    }
}
