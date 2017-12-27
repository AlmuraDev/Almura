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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientboundPlayerSelectedTitlesPacket implements Message {

    public Map<UUID, Text> titles;

    public ClientboundPlayerSelectedTitlesPacket() {
    }

    public ClientboundPlayerSelectedTitlesPacket(Map<UUID, Text> titles) {
        this.titles = titles;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.titles = new HashMap<>();

        final int count = buf.readInteger();

        for (int i = 0; i < count; i++) {
            final UUID uniqueId = buf.readUniqueId();
            final Text title = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString());

            this.titles.put(uniqueId, title);
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.titles.size());
        for (Map.Entry<UUID, Text> entry : this.titles.entrySet()) {
            buf.writeUniqueId(entry.getKey());
            buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(entry.getValue()));
        }
    }
}
