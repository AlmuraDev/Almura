/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.shared.util.TextUtil;
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
        String StrPacket1 = buf.readString();
        String StrPacket2 = buf.readString();
        String StrPacket3 = buf.readString();
        String StrPacket4 = buf.readString();
        final String content = StrPacket1 + StrPacket2 + StrPacket3 + StrPacket4;

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

        if (TextUtil.asUglyText(page.getContent()).length() <= 8190) {
            buf.writeString(TextUtil.asUglyText(page.getContent()));
            buf.writeString("");
            buf.writeString("");
            buf.writeString("");
        }

        if (TextUtil.asUglyText(page.getContent()).length() > 8190 && TextUtil.asUglyText(page.getContent()).length() <= 16382) {
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(0, 8190));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(8191, page.getContent().length()));
            buf.writeString("");
            buf.writeString("");
        }

        if (TextUtil.asUglyText(page.getContent()).length() > 16382 && TextUtil.asUglyText(page.getContent()).length() <= 24572) {
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(0, 8190));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(8191, 16382));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(16383, page.getContent().length()));
            buf.writeString("");
        }

        if (TextUtil.asUglyText(page.getContent()).length() > 24572 && TextUtil.asUglyText(page.getContent()).length() <= 32762) {
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(0, 8190));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(8191, 16382));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(16383, 24572));
            buf.writeString(TextUtil.asUglyText(page.getContent()).substring(24573, page.getContent().length()));
        }
    }
}
