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

public final class ClientboundNicknameOpenResponsePacket implements Message {

    public boolean canChangeNickname, isAdmin;

    public ClientboundNicknameOpenResponsePacket() {
    }

    public ClientboundNicknameOpenResponsePacket(boolean canChangeNickname, boolean isAdmin) {
        this.canChangeNickname = canChangeNickname;
        this.isAdmin = isAdmin;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.canChangeNickname = buf.readBoolean();
        this.isAdmin = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.canChangeNickname);
        buf.writeBoolean(this.isAdmin);
    }
}
