/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundSelectedTitleBulkPacket implements Message {

    @Nullable public Map<UUID, String> titles;

    public ClientboundSelectedTitleBulkPacket() {
    }

    public ClientboundSelectedTitleBulkPacket(@Nullable final Map<UUID, String> titles) {
        this.titles = titles;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {

        final int count = buf.readVarInt();

        if (count > 0) {
            this.titles = new HashMap<>();

            for (int i = 0; i < count; i++) {
                final UUID uniqueId = buf.readUniqueId();
                final String id = buf.readString();

                this.titles.put(uniqueId, id);
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeVarInt(this.titles == null ? 0 : this.titles.size());

        if (this.titles != null) {
            for (Map.Entry<UUID, String> entry : this.titles.entrySet()) {
                buf.writeUniqueId(entry.getKey());
                buf.writeString(entry.getValue());
            }
        }
    }
}
