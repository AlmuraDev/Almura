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
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.util.SerializationUtil;
import net.minecraft.item.Item;
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

  public ClientboundForSaleItemsResponsePacket() {
  }

  public ClientboundForSaleItemsResponsePacket(final String id, final List<ForSaleItem> forSaleItems) {
    checkNotNull(id);
    checkNotNull(forSaleItems);

    this.id = id;
    this.forSaleItems = forSaleItems;
  }

  @Override
  public void readFrom(final ChannelBuf buf) {
    this.id = buf.readString();
    final int count = buf.readInteger();

    if (count > 0) {
      this.forSaleItems = new ArrayList<>();

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

        final Instant listItemCreated;
        try {
          listItemCreated = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
        } catch (IOException | ClassNotFoundException e) {
          // TODO Malformed ListItem created date
          continue;
        }

        final UUID seller = buf.readUniqueId();
        final String sellerName = buf.readBoolean() ? buf.readString() : null;

        final BigDecimal price = SerializationUtil.fromBytes(buf.readBytes(buf.readInteger()));
        final int index = buf.readInteger();

        final BasicListItem basicListItem = new BasicListItem(record, listItemCreated, seller, item, quantity, metadata, price, index);

        if (Sponge.getPlatform().getExecutionType().isClient()) {
          basicListItem.setSellerName(sellerName);
        }

        // ForSaleItem
        final Instant forSaleItemCreated;
        try {
          forSaleItemCreated = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
        } catch (IOException | ClassNotFoundException e) {
          // TODO Malformed ForSaleItem created date
          continue;
        }

        final int soldQuantity = buf.readInteger();

        final BasicForSaleItem basicForSaleItem = new BasicForSaleItem(basicListItem, forSaleItemCreated, soldQuantity);
        this.forSaleItems.add(basicForSaleItem);
      }
    }
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
          // TODO Bad item, no location
          continue;
        }

        final byte[] listItemCreatedData;
        try {
          listItemCreatedData = SerializationUtil.objectToBytes(listItem.getCreated());
        } catch (IOException e) {
          // TODO Malformed ListItem created date
          continue;
        }

        final byte[] forSaleItemCreatedData;
        try {
          forSaleItemCreatedData = SerializationUtil.objectToBytes(forSaleItem.getCreated());
        } catch (IOException e) {
          // TODO Malformed ForSaleItem created date
          continue;
        }

        // ListItem
        buf.writeInteger(listItem.getRecord());

        buf.writeString(SerializationUtil.toString(location));

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

        final byte[] priceData = SerializationUtil.toBytes(listItem.getPrice());
        buf.writeInteger(priceData.length);
        buf.writeBytes(priceData);

        buf.writeInteger(listItem.getIndex());

        // ForSaleItem
        buf.writeInteger(forSaleItemCreatedData.length);
        buf.writeBytes(forSaleItemCreatedData);

        buf.writeInteger(forSaleItem.getQuantity());
      }
    }
  }
}
