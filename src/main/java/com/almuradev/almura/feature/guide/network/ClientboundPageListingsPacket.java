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

import java.util.ArrayList;
import java.util.List;

public final class ClientboundPageListingsPacket implements Message {

    public List<PageListEntry> pageEntries = new ArrayList<>();
    public boolean shouldSwitchToPage;
    public String switchToPageId;
    // Only the order changed (a page was moved): the client keeps the page it has open instead of opening it again
    public boolean reorderOnly;

    public ClientboundPageListingsPacket() {
    }

    public ClientboundPageListingsPacket(List<PageListEntry> pageEntries, String switchToPageId) {
        this(pageEntries, switchToPageId, false);
    }

    public ClientboundPageListingsPacket(List<PageListEntry> pageEntries, String switchToPageId, boolean reorderOnly) {
        this.pageEntries = pageEntries;
        if (switchToPageId != null) {
            this.shouldSwitchToPage = true;
        }
        this.switchToPageId = switchToPageId;
        this.reorderOnly = reorderOnly;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            final String id = buf.readString();
            final String name = buf.readString();
            final boolean playerGuide = buf.readBoolean();
            final boolean mine = playerGuide && buf.readBoolean();
            final String ownerName = playerGuide ? buf.readString() : "";

            this.pageEntries.add(new PageListEntry(id, name, playerGuide, mine, ownerName));
        }

        this.shouldSwitchToPage = buf.readBoolean();
        if (this.shouldSwitchToPage) {
            this.switchToPageId = buf.readString();
        }
        this.reorderOnly = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.pageEntries.size());
        this.pageEntries.forEach((entry) -> {
            buf.writeString(entry.getId());
            buf.writeString(entry.getName());
            buf.writeBoolean(entry.isPlayerGuide());
            if (entry.isPlayerGuide()) {
                buf.writeBoolean(entry.isMine());
                buf.writeString(entry.getOwnerName());
            }
        });
        buf.writeBoolean(this.shouldSwitchToPage);
        if (this.shouldSwitchToPage) {
            buf.writeString(this.switchToPageId);
        }
        buf.writeBoolean(this.reorderOnly);
    }
}
