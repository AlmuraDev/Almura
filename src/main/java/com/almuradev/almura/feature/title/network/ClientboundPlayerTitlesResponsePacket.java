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

import java.util.LinkedHashSet;
import java.util.Set;

public final class ClientboundPlayerTitlesResponsePacket implements Message {

    public int selectedIndex;
    public Set<Text> titles;

    public ClientboundPlayerTitlesResponsePacket() {
    }

    public ClientboundPlayerTitlesResponsePacket(int selectedIndex, Set<Text> titles) {
        this.selectedIndex = selectedIndex;
        this.titles = titles;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.selectedIndex = buf.readInteger();

        this.titles = new LinkedHashSet<>();

        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.titles.add(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString()));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.selectedIndex);

        final int count = this.titles.size();

        buf.writeInteger(count);
        for (Text title : this.titles) {
            buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(title));
        }
    }
}
