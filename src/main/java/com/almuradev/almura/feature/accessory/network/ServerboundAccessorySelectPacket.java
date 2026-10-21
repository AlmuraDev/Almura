/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * Wears a variant of an accessory type, or takes the type off when the variant is empty.
 */
public final class ServerboundAccessorySelectPacket implements Message {

    public String type = "";
    public String variant = "";

    public ServerboundAccessorySelectPacket() {
    }

    public ServerboundAccessorySelectPacket(final String type, final String variant) {
        this.type = type;
        this.variant = variant;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = buf.readString();
        this.variant = buf.readString();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.type);
        buf.writeString(this.variant);
    }
}
