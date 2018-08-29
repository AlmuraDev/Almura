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
import com.almuradev.almura.feature.exchange.basic.listing.BasicForSaleItem;
import com.almuradev.almura.feature.exchange.basic.listing.BasicListItem;
import com.almuradev.almura.shared.util.ByteBufUtil;
import com.almuradev.almura.shared.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
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

public final class ClientboundForSaleItemsResponsePacket implements Message {

    @Nullable public String id;
    @Nullable public List<ForSaleItem> forSaleItems;
    public int preLimitCount;

    public ClientboundForSaleItemsResponsePacket() {
    }

    public ClientboundForSaleItemsResponsePacket(final String id, @Nullable final List<ForSaleItem> forSaleItems, final int preLimitCount) {
        checkNotNull(id);
        checkNotNull(forSaleItems);

        this.id = id;
        this.forSaleItems = forSaleItems;
        this.preLimitCount = preLimitCount;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        final int count = buf.readInteger();

        if (count > 0) {
            this.forSaleItems = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final int listItemRecNo = buf.readInteger();
                final ResourceLocation location = new ResourceLocation(buf.readString(), buf.readString());

                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    new IOException("Unknown item id when receiving for sale item! Id [" + location + "]. Skipping...").printStackTrace();
                    continue;
                }

                final int quantity = buf.readInteger();
                final int metadata = buf.readInteger();

                final Instant listItemCreated;
                try {
                    listItemCreated = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                final UUID seller = buf.readUniqueId();
                final String sellerName = buf.readBoolean() ? buf.readString() : null;

                final int index = buf.readInteger();

                BigDecimal lastKnownPrice = null;
                if (buf.readBoolean()) {
                    lastKnownPrice = ByteBufUtil.readBigDecimal((ByteBuf) buf);
                }

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

                final BasicListItem basicListItem =
                    new BasicListItem(listItemRecNo, listItemCreated, seller, item, quantity, metadata, index, lastKnownPrice, compound);

                if (Sponge.getPlatform().getExecutionType().isClient()) {
                    basicListItem.setSellerName(sellerName);
                }

                final int forSaleItemRecNo = buf.readInteger();

                // ForSaleItem
                final Instant forSaleItemCreated;
                try {
                    forSaleItemCreated = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                final BigDecimal price = ByteBufUtil.readBigDecimal((ByteBuf) buf);

                final BasicForSaleItem basicForSaleItem = new BasicForSaleItem(basicListItem, forSaleItemRecNo, forSaleItemCreated, price);
                this.forSaleItems.add(basicForSaleItem);
            }
        }

        this.preLimitCount = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);

        buf.writeString(this.id);
        buf.writeInteger(this.forSaleItems == null ? 0 : this.forSaleItems.size());

        if (this.forSaleItems != null) {
            for (final ForSaleItem forSaleItem : this.forSaleItems) {
                final ListItem listItem = forSaleItem.getListItem();
                final ResourceLocation location = listItem.getItem().getRegistryName();
                if (location == null) {
                    new IOException("Malformed location when sending for sale item! Id [" + listItem.getItem().getRegistryName() + "].").printStackTrace();
                    continue;
                }

                final byte[] listItemCreatedData;
                try {
                    listItemCreatedData = SerializationUtil.objectToBytes(listItem.getCreated());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final NBTTagCompound compound = listItem.getCompound();
                byte[] compoundData = null;

                if (compound != null) {
                    try {
                        compoundData = SerializationUtil.toBytes(compound);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                final byte[] forSaleItemCreatedData;
                try {
                    forSaleItemCreatedData = SerializationUtil.objectToBytes(forSaleItem.getCreated());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                // ListItem
                buf.writeInteger(listItem.getRecord());

                buf.writeString(location.getNamespace());
                buf.writeString(location.getPath());

                buf.writeInteger(listItem.getQuantity());
                buf.writeInteger(listItem.getMetadata());

                buf.writeInteger(listItemCreatedData.length);
                buf.writeBytes(listItemCreatedData);

                buf.writeUniqueId(listItem.getSeller());

                final String sellerName = listItem.getSellerName().orElse(null);
                buf.writeBoolean(sellerName != null);

                if (sellerName != null) {
                    buf.writeString(sellerName);
                }

                buf.writeInteger(listItem.getIndex());

                buf.writeBoolean(listItem.getLastKnownPrice().isPresent());
                if (listItem.getLastKnownPrice().isPresent()) {
                    ByteBufUtil.writeBigDecimal((ByteBuf) buf, listItem.getLastKnownPrice().get());
                }

                if (compoundData == null) {
                    buf.writeInteger(0);
                } else {
                    buf.writeInteger(compoundData.length);
                    buf.writeBytes(compoundData);
                }

                // ForSaleItem
                buf.writeInteger(forSaleItem.getRecord());
                buf.writeInteger(forSaleItemCreatedData.length);
                buf.writeBytes(forSaleItemCreatedData);

                ByteBufUtil.writeBigDecimal((ByteBuf) buf, forSaleItem.getPrice());
            }
        }

        buf.writeInteger(this.preLimitCount);
    }
}
