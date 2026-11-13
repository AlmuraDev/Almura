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

import javax.annotation.Nullable;

public final class ServerboundSelectedTitlePacket implements Message {

    @Nullable public String titleId;

    public ServerboundSelectedTitlePacket() {
    }

    public ServerboundSelectedTitlePacket(@Nullable final String titleId) {
        this.titleId = titleId;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        if (buf.readBoolean()) {
            this.titleId = buf.readString();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.titleId != null);

        if (this.titleId != null) {
            buf.writeString(this.titleId);
        }
    }
}
