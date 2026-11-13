/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundSelectedTitlePacket implements Message {

    @Nullable public UUID uniqueId;
    @Nullable public String titleId;

    public ClientboundSelectedTitlePacket() {
    }

    public ClientboundSelectedTitlePacket(final UUID uniqueId, @Nullable final String titleId) {
        checkNotNull(uniqueId);

        this.uniqueId = uniqueId;
        this.titleId = titleId;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.uniqueId = buf.readUniqueId();

        if (buf.readBoolean()) {
            this.titleId = buf.readString();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.uniqueId);

        buf.writeUniqueId(this.uniqueId);
        buf.writeBoolean(this.titleId != null);

        if (this.titleId != null) {
            buf.writeString(this.titleId);
        }
    }
}
