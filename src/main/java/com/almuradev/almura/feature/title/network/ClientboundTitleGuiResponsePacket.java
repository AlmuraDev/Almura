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

    public boolean canChangeTitle, isAdmin;

    public ClientboundTitleGuiResponsePacket() {
    }

    public ClientboundTitleGuiResponsePacket(boolean canChangeTitle, boolean isAdmin) {
        this.canChangeTitle = canChangeTitle;
        this.isAdmin = isAdmin;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.canChangeTitle = buf.readBoolean();
        this.isAdmin = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.canChangeTitle);
        buf.writeBoolean(this.isAdmin);
    }
}
