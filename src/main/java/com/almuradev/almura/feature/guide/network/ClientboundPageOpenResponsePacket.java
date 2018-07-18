/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.Page;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.time.Instant;
import java.util.UUID;

public final class ClientboundPageOpenResponsePacket implements Message {

    public Page page;

    public ClientboundPageOpenResponsePacket() {
    }

    public ClientboundPageOpenResponsePacket(Page page) {
        this.page = page;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        // Page information
        final String id = buf.readString();
        final int index = buf.readInteger();
        final String name = buf.readString();
        final UUID creator = buf.readUniqueId();
        final Instant created = Instant.parse(buf.readString());
        final UUID lastModifier = buf.readUniqueId();
        final Instant lastModified = Instant.parse(buf.readString());
        final String content = buf.readString();

        this.page = new Page(id, creator, created);
        this.page.setIndex(index);
        this.page.setName(name);
        this.page.setLastModifier(lastModifier);
        this.page.setLastModified(lastModified);
        this.page.setContent(content);
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        // Page information
        buf.writeString(page.getId());
        buf.writeInteger(page.getIndex());
        buf.writeString(page.getName());
        buf.writeUniqueId(page.getCreator());
        buf.writeString(page.getCreated().toString());
        buf.writeUniqueId(page.getLastModifier());
        buf.writeString(page.getLastModified().toString());
        buf.writeString(Page.asUglyText(page.getContent()));
    }
}
