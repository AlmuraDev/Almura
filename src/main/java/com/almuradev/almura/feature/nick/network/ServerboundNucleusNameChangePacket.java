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

public final class ServerboundNucleusNameChangePacket implements Message {

    public String nickname;

    public ServerboundNucleusNameChangePacket() {
    }

    public ServerboundNucleusNameChangePacket(final String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.nickname = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.nickname);
    }
}
