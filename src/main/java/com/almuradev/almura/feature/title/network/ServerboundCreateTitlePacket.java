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

import javax.annotation.Nullable;

public final class ServerboundCreateTitlePacket implements Message {

    @Nullable public String id, name, permission, content;

    public ServerboundCreateTitlePacket() {

    }

    public ServerboundCreateTitlePacket(final String id, final String name, final String permission, final String content) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);
        checkNotNull(content);

        this.id = id;
        this.name = name;
        this.permission = permission;
        this.content = content;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();
        this.name = buf.readString();
        this.permission = buf.readString();
        this.content = buf.readString();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.name);
        checkNotNull(this.permission);
        checkNotNull(this.content);

        buf.writeString(this.id);
        buf.writeString(this.name);
        buf.writeString(this.permission);
        buf.writeString(this.content);
    }
}
