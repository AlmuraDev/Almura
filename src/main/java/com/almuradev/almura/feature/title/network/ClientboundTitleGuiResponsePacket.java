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

public final class ClientboundTitleGuiResponsePacket implements Message {

    public boolean isAdmin;

    public ClientboundTitleGuiResponsePacket() {
    }

    public ClientboundTitleGuiResponsePacket(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.isAdmin = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.isAdmin);
    }
}
