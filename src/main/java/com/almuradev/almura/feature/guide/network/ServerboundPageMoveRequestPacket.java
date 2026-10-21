/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * Moves a page one place up (-1) or down (1) the list, among the pages of its kind (server guides, or the player's own guides).
 */
public final class ServerboundPageMoveRequestPacket implements Message {

    public String id;
    public int direction;

    public ServerboundPageMoveRequestPacket() {
    }

    public ServerboundPageMoveRequestPacket(String id, int direction) {
        this.id = id;
        this.direction = direction;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.id = buf.readString();
        this.direction = buf.readByte();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.id);
        buf.writeByte((byte) this.direction);
    }
}
