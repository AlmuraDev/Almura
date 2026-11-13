/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.store.StoreItemSegmentType;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public final class ServerboundListItemsRequestPacket implements Message {

    @Nullable public String id;
    @Nullable public StoreItemSegmentType type;
    @Nullable public List<ListCandidate> candidates;

    public ServerboundListItemsRequestPacket() {

    }

    public ServerboundListItemsRequestPacket(final String id, final StoreItemSegmentType type, @Nullable final List<ListCandidate>
        candidates) {
        this.id = checkNotNull(id);
        this.type = checkNotNull(type);
        this.candidates = candidates;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.type = StoreItemSegmentType.valueOf(buf.readString());
        final int count = buf.readInteger();

        if (count > 0) {
            this.candidates = new ArrayList<>();

            for (int i = 0; i < count; i++) {
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

                this.candidates.add(new ListCandidate(new BasicVanillaStack(item, quantity, metadata, compound), index, price));
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.type);

        buf.writeString(this.id);
        String type = this.type.name().toUpperCase(Locale.ENGLISH);
        buf.writeString(type);
        buf.writeInteger(this.candidates == null ? 0 : this.candidates.size());

        if (this.candidates != null) {
            for (final ListCandidate candidate : this.candidates) {

                final ResourceLocation location = candidate.stack.getItem().getRegistryName();
                if (location == null) {
                    new IOException("Malformed resource location for Item '" + candidate.stack.getItem() + "' when sending " + type + " item!")
                        .printStackTrace();
                    continue;
                }

                final NBTTagCompound compound = candidate.stack.getCompound();
                byte[] compoundData = null;

                if (compound != null) {
                    try {
                        compoundData = SerializationUtil.toBytes(compound);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                buf.writeString(location.getNamespace());
                buf.writeString(location.getPath());

                buf.writeInteger(candidate.stack.getQuantity());
                buf.writeInteger(candidate.stack.getMetadata());

                ByteBufUtil.writeBigDecimal((ByteBuf) buf, candidate.price);

                buf.writeInteger(candidate.index);

                if (compoundData == null) {
                    buf.writeInteger(0);
                } else {
                    buf.writeInteger(compoundData.length);
                    buf.writeBytes(compoundData);
                }
            }
        }
    }

    public static class ListCandidate {
        public final VanillaStack stack;
        public final int index;
        public BigDecimal price;

        public ListCandidate(final VanillaStack stack, final int index, final BigDecimal price) {
            checkNotNull(stack);
            checkState(stack.getQuantity() >= FeatureConstants.UNLIMITED);
            checkState(index >= 0);
            checkNotNull(price);
            checkState(price.doubleValue() >= 0);

            this.stack = stack;
            this.index = index;
            this.price = price;
        }
    }
}
