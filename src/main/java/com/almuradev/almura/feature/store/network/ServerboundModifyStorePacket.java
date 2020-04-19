/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.store.StoreModifyType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundModifyStorePacket implements Message {

    @Nullable public StoreModifyType type;
    @Nullable public String id, name, permission;
    public boolean isHidden;

    public ServerboundModifyStorePacket() {
    }

    public ServerboundModifyStorePacket(final StoreModifyType type, final String id, @Nullable final String name, @Nullable final String
        permission, final boolean isHidden) {
        checkNotNull(type);
        checkNotNull(id);

        this.type = type;
        this.id = id;

        if (this.type != StoreModifyType.DELETE) {
            checkNotNull(name);
            checkNotNull(permission);
        }

        this.name = name;
        this.permission = permission;
        this.isHidden = isHidden;
    }

    public ServerboundModifyStorePacket(final String id) {
        this(StoreModifyType.DELETE, id, null, null, false);
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = StoreModifyType.valueOf(buf.readString());
        this.id = buf.readString();

        if (this.type != StoreModifyType.DELETE) {
            this.name = buf.readString();
            this.permission = buf.readString();
            this.isHidden = buf.readBoolean();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);
        checkNotNull(this.id);

        if (this.type != StoreModifyType.DELETE) {
            checkNotNull(this.name);
            checkNotNull(this.permission);
        }

        buf.writeString(this.type.name().toUpperCase());
        buf.writeString(this.id);

        if (this.type != StoreModifyType.DELETE) {
            buf.writeString(this.name);
            buf.writeString(this.permission);
            buf.writeBoolean(this.isHidden);
        }
    }
}
