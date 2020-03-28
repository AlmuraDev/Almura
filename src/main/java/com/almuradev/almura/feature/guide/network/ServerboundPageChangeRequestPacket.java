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

public final class ServerboundPageChangeRequestPacket implements Message {

    public PageChangeType changeType;
    public String id, name, content;
    public int index;

    public ServerboundPageChangeRequestPacket() {
    }

    public ServerboundPageChangeRequestPacket(Page page) {
        this.changeType = PageChangeType.MODIFY;
        this.id = page.getId();
        this.index = page.getIndex();
        this.name = page.getName();
        this.content = page.getContent();
    }

    public ServerboundPageChangeRequestPacket(String id, int index, String name) {
        this.changeType = PageChangeType.ADD;
        this.id = id;
        this.index = index;
        this.name = name;
        this.content = "";
    }

    public ServerboundPageChangeRequestPacket(String id) {
        this.changeType = PageChangeType.REMOVE;
        this.id = id;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.changeType = PageChangeType.of(buf.readByte());
        if (this.changeType != null) {
            this.id = buf.readString();
            if (this.changeType != PageChangeType.REMOVE) {
                this.index = buf.readInteger();
                this.name = buf.readString();
                String StrPacket1 = buf.readString();
                String StrPacket2 = buf.readString();
                String StrPacket3 = buf.readString();
                String StrPacket4 = buf.readString();
                this.content = StrPacket1 + StrPacket2 + StrPacket3 + StrPacket4;
            }
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeByte((byte) this.changeType.ordinal());
        buf.writeString(this.id);
        if (this.changeType != PageChangeType.REMOVE) {
            buf.writeInteger(this.index);
            buf.writeString(this.name);

            if (this.content.length() <= 8190) {
                buf.writeString(this.content);
                buf.writeString("");
                buf.writeString("");
                buf.writeString("");
            }

            if (this.content.length() > 8190 && this.content.length() <= 16382) {
                buf.writeString(this.content.substring(0, 8190));
                buf.writeString(this.content.substring(8191, this.content.length()));
                buf.writeString("");
                buf.writeString("");
            }

            if (this.content.length() > 16382 && this.content.length() <= 24572) {
                buf.writeString(this.content.substring(0, 8190));
                buf.writeString(this.content.substring(8191, 16382));
                buf.writeString(this.content.substring(16383, this.content.length()));
                buf.writeString("");
            }

            if (this.content.length() > 24572 && this.content.length() <= 32762) {
                buf.writeString(this.content.substring(0, 8190));
                buf.writeString(this.content.substring(8191, 16382));
                buf.writeString(this.content.substring(16383, 24572));
                buf.writeString(this.content.substring(24573, this.content.length()));
            }
        }
    }
}
