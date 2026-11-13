/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundNucleusNameChangePacket implements Message {

    @Nullable public String nickname;

    public ServerboundNucleusNameChangePacket() {
    }

    public ServerboundNucleusNameChangePacket(@Nullable final String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.nickname = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        if (this.nickname != null) {
            buf.writeString(this.nickname);
        } else {
            buf.writeString(""); //Can't send null in a string.
        }
    }
}
