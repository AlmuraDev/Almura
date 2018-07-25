/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.TitleGuiType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundTitleGuiRequestPacket implements Message {

    @Nullable public TitleGuiType type;

    public ServerboundTitleGuiRequestPacket() {
    }

    public ServerboundTitleGuiRequestPacket(final TitleGuiType type) {
        this.type = type;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.type = TitleGuiType.valueOf(buf.readString());
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        checkNotNull(this.type);

        buf.writeString(this.type.name().toUpperCase());
    }
}
