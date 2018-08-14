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
                final String rawItemId = buf.readString();
                final ResourceLocation location = SerializationUtil.fromString(rawItemId);
                if (location == null) {
                    new IOException("Malformed item id when receiving list item! Id [" + rawItemId + "]. Skipping...").printStackTrace();
                    continue;
                }

                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    new IOException("Unknown item id when receiving list item! Id [" + rawItemId + "]. Skipping...").printStackTrace();
                    continue;
                }

                final int quantity = buf.readInteger();
                final int metadata = buf.readInteger();

                final Instant created;
                try {
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                final UUID seller = buf.readUniqueId();
                final String sellerName = buf.readBoolean() ? buf.readString() : null;

                final int index = buf.readInteger();

                final int compoundDataLength = buf.readInteger();
                NBTTagCompound compound = null;

                if (compoundDataLength > 0) {
                    try {
                        compound = SerializationUtil.compoundFromBytes(buf.readBytes(compoundDataLength));
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                final BasicListItem basicListItem = new BasicListItem(record, created, seller, item, quantity, metadata, index, compound);

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
                    new IOException("Malformed location when sending list item! Item [" + item.getItem() + "].").printStackTrace();
                    continue;
                }

                final byte[] createdData;
                try {
                    createdData = SerializationUtil.objectToBytes(item.getCreated());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final NBTTagCompound compound = item.getCompound();
                byte[] compoundData = null;

                if (compound != null) {
                    try {
                        compoundData = SerializationUtil.toBytes(compound);
                    } catch (IOException e) {
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
