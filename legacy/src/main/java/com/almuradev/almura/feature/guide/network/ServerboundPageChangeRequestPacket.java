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

    private static final int CONTENT_PIECES = 4;
    private static final int CONTENT_PIECE_LENGTH = 8191;
    // The longest content that fits in the pieces
    public static final int MAX_CONTENT_LENGTH = CONTENT_PIECES * CONTENT_PIECE_LENGTH;

    public PageChangeType changeType;
    public String id, name, content;
    public int index;
    // ADD only: a player guide of the sender (the server picks its id) instead of a server guide
    public boolean playerGuide;

    public ServerboundPageChangeRequestPacket() {
    }

    public ServerboundPageChangeRequestPacket(Page page) {
        this.changeType = PageChangeType.MODIFY;
        this.id = page.getId();
        this.index = page.getIndex();
        this.name = page.getName();
        this.content = page.getContent();
    }

    public ServerboundPageChangeRequestPacket(String id, int index, String name, boolean playerGuide) {
        this.changeType = PageChangeType.ADD;
        this.id = id;
        this.index = index;
        this.name = name;
        this.content = "";
        this.playerGuide = playerGuide;
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
                if (this.changeType == PageChangeType.ADD) {
                    this.playerGuide = buf.readBoolean();
                }
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

            // Strings are limited in size, the content goes up in 4 back to back pieces
            for (int i = 0; i < CONTENT_PIECES; i++) {
                final int start = Math.min(i * CONTENT_PIECE_LENGTH, this.content.length());
                final int end = Math.min(start + CONTENT_PIECE_LENGTH, this.content.length());
                buf.writeString(this.content.substring(start, end));
            }

            if (this.changeType == PageChangeType.ADD) {
                buf.writeBoolean(this.playerGuide);
            }
        }
    }
}
