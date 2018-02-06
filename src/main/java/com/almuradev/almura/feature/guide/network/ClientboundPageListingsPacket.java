/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.PageListEntry;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ClientboundPageListingsPacket implements Message {

    public Set<PageListEntry> pageEntries = new LinkedHashSet<>();

    public ClientboundPageListingsPacket() {
    }

    public ClientboundPageListingsPacket(Set<PageListEntry> pageEntries) {
        this.pageEntries = pageEntries;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            final String id = buf.readString();
            final String name = buf.readString();

            this.pageEntries.add(new PageListEntry(id, name));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.pageEntries.size());
        this.pageEntries.forEach((entry) -> {
            buf.writeString(entry.getId());
            buf.writeString(entry.getName());
        });
    }
}
