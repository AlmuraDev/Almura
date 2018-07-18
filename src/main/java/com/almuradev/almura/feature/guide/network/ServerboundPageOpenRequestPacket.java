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

public final class ServerboundPageOpenRequestPacket implements Message {

    public String id;

    public ServerboundPageOpenRequestPacket() {
    }

    public ServerboundPageOpenRequestPacket(String id) {
        this.id = id;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.id = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.id);
    }
}
