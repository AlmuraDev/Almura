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
import com.almuradev.almura.shared.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public final class ServerboundModifyItemsPacket implements Message {

    @Nullable public String id;
    @Nullable public StoreItemSegmentType type;
    @Nullable public List<ModifyCandidate> candidates;

    public ServerboundModifyItemsPacket() {

    }

    public ServerboundModifyItemsPacket(final String id, final StoreItemSegmentType type, @Nullable final List<ModifyCandidate>
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
                final int recNo = buf.readInteger();
                final int quantity = buf.readInteger();
                final int index = buf.readInteger();
                final BigDecimal price = ByteBufUtil.readBigDecimal((ByteBuf) buf);

                this.candidates.add(new ModifyCandidate(recNo, quantity, index, price));
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
            for (final ModifyCandidate candidate : this.candidates) {
                buf.writeInteger(candidate.recNo);
                buf.writeInteger(candidate.quantity);
                buf.writeInteger(candidate.index);
                ByteBufUtil.writeBigDecimal((ByteBuf) buf, candidate.price);
            }
        }
    }

    public static class ModifyCandidate {
        public final int recNo;
        public final int quantity;
        public final int index;
        public final BigDecimal price;

        public ModifyCandidate(final int recNo, final int quantity, final int index, final BigDecimal price) {
            checkState(recNo >= 0);
            checkState(quantity >= FeatureConstants.UNLIMITED);
            checkState(index >= 0);
            checkNotNull(price);
            checkState(price.doubleValue() >= 0);

            this.recNo = recNo;
            this.quantity = quantity;
            this.index = index;
            this.price = price;
        }
    }
}
