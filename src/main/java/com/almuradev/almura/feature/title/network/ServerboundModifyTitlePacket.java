/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.TitleModifyType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundModifyTitlePacket implements Message {

    @Nullable public TitleModifyType type;
    @Nullable public String id, name, permission, content;
    public boolean isHidden = false;

    public ServerboundModifyTitlePacket() {

    }

    public ServerboundModifyTitlePacket(final TitleModifyType type, final String id, @Nullable final String name, @Nullable final String
        permission, @Nullable final String content, final boolean isHidden) {
        checkNotNull(type);
        checkNotNull(id);

        if (type != TitleModifyType.DELETE) {
            checkNotNull(name);
            checkNotNull(permission);
            checkNotNull(content);
        }

        this.type = type;
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.content = content;
        this.isHidden = isHidden;
    }

    public ServerboundModifyTitlePacket(final String id) {
        this(TitleModifyType.DELETE, id, null, null, null, false);
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.type = TitleModifyType.valueOf(buf.readString());
        this.id = buf.readString();

        if (this.type != TitleModifyType.DELETE) {
            this.name = buf.readString();
            this.permission = buf.readString();
            this.content = buf.readString();
            this.isHidden = buf.readBoolean();
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.type);
        checkNotNull(this.id);

        if (this.type != TitleModifyType.DELETE) {
            checkNotNull(this.name);
            checkNotNull(this.permission);
            checkNotNull(this.content);
        }

        buf.writeString(this.type.name().toUpperCase());
        buf.writeString(this.id);

        if (this.type != TitleModifyType.DELETE) {
            buf.writeString(this.name);
            buf.writeString(this.permission);
            buf.writeString(this.content);
            buf.writeBoolean(this.isHidden);
        }
    }
}
