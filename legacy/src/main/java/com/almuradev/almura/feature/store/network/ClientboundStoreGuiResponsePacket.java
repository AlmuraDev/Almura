/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.store.StoreGuiType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ClientboundStoreGuiResponsePacket implements Message {

    @Nullable public StoreGuiType type;
    @Nullable public String id;
    public boolean isAdmin;

    public ClientboundStoreGuiResponsePacket() {

    }

    public ClientboundStoreGuiResponsePacket(final StoreGuiType type) {
        this(type, null, false);
    }

    public ClientboundStoreGuiResponsePacket(final StoreGuiType type, @Nullable final String id, final boolean isAdmin) {
        checkNotNull(type);

        this.type = type;

        if (this.type != StoreGuiType.MANAGE) {
            checkNotNull(id);
            this.id = id;
            this.isAdmin = isAdmin;
        }
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = StoreGuiType.valueOf(buf.readString());

        if (this.type != StoreGuiType.MANAGE) {
            this.id = buf.readString();
            this.isAdmin = buf.readBoolean();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);

        buf.writeString(this.type.name().toUpperCase());

        if (this.type != StoreGuiType.MANAGE) {
            checkNotNull(this.id);

            buf.writeString(this.id);
            buf.writeBoolean(this.isAdmin);
        }
    }
}
