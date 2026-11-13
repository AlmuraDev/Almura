/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.store.StoreItemSegmentType;
import com.almuradev.almura.feature.store.basic.listing.AbstractStoreItem;
import com.almuradev.almura.feature.store.basic.listing.BasicSellingItem;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.util.ByteBufUtil;
import com.almuradev.almura.shared.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public final class ClientboundListItemsResponsePacket implements Message {

    @Nullable public String id;
    @Nullable public StoreItemSegmentType type;
    @Nullable public List<? extends StoreItem> storeItems;

    public ClientboundListItemsResponsePacket() {

    }

    public ClientboundListItemsResponsePacket(final String id, final StoreItemSegmentType type, @Nullable final List<? extends StoreItem> storeItems) {
        this.id = checkNotNull(id);
        this.type = checkNotNull(type);
        this.storeItems = storeItems;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.type = StoreItemSegmentType.valueOf(buf.readString());
        final int count = buf.readInteger();

        if (count > 0) {
            if (type == StoreItemSegmentType.SELLING) {
                this.storeItems = new ArrayList<SellingItem>();
            } else {
                this.storeItems = new ArrayList<BuyingItem>();
            }

            for (int i = 0; i < count; i++) {
                final int record = buf.readInteger();

                final Instant created;
                try {
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                final ResourceLocation location = new ResourceLocation(buf.readString(), buf.readString());

                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    new IOException("Unknown item id '" + location.toString() + "' when receiving item! . Skipping...").printStackTrace();
                    continue;
                }

                final int quantity = buf.readInteger();
                final int metadata = buf.readInteger();

                final BigDecimal price = ByteBufUtil.readBigDecimal((ByteBuf) buf);

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

                final Constructor<? extends AbstractStoreItem> constructor;
                try {
                    constructor = this.type.getItemClass().getConstructor(Integer.TYPE, Instant.class, Item.class, Integer.TYPE, Integer.TYPE,
                        BigDecimal.class, Integer.TYPE, NBTTagCompound.class);
                    final AbstractStoreItem storeItem = constructor.newInstance(record, created, item, quantity, metadata, price, index, compound);

                    if (this.type == StoreItemSegmentType.SELLING) {
                        ((List<SellingItem>) this.storeItems).add((SellingItem) storeItem);
                    } else {
                        ((List<BuyingItem>) this.storeItems).add((BuyingItem) storeItem);
                    }

                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.type);

        buf.writeString(this.id);
        buf.writeString(this.type.name().toUpperCase(Locale.ENGLISH));
        buf.writeInteger(this.storeItems == null ? 0 : this.storeItems.size());

        if (this.storeItems != null) {
            for (final StoreItem item : this.storeItems) {

                final ResourceLocation location = item.getItem().getRegistryName();
                if (location == null) {
                    new IOException("Malformed resource location for Item '" + item + "' when sending list item!").printStackTrace();
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

                buf.writeInteger(createdData.length);
                buf.writeBytes(createdData);

                buf.writeString(location.getNamespace());
                buf.writeString(location.getPath());

                buf.writeInteger(item.getQuantity());
                buf.writeInteger(item.getMetadata());

                ByteBufUtil.writeBigDecimal((ByteBuf) buf, item.getPrice());

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
