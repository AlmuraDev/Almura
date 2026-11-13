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
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public final class ServerboundDelistItemsPacket implements Message {

    @Nullable public String id;
    @Nullable public StoreItemSegmentType type;
    @Nullable public List<Integer> recNos;

    public ServerboundDelistItemsPacket() {

    }

    public ServerboundDelistItemsPacket(final String id, final StoreItemSegmentType type, @Nullable final List<Integer>
        recNos) {
        this.id = checkNotNull(id);
        this.type = checkNotNull(type);
        this.recNos = recNos;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.type = StoreItemSegmentType.valueOf(buf.readString());
        final int count = buf.readInteger();

        if (count > 0) {
            this.recNos = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final int recNo = buf.readInteger();

                this.recNos.add(recNo);
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
        buf.writeInteger(this.recNos == null ? 0 : this.recNos.size());

        if (this.recNos != null) {
            for (final Integer recNo : this.recNos) {
                buf.writeInteger(recNo);
            }
        }
    }
}
