/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundGuideOpenResponsePacket implements Message {

    // Global permissions
    public boolean canAdd;
    public boolean canRemove;
    public boolean canModify;

    public ClientboundGuideOpenResponsePacket() {
    }

    public ClientboundGuideOpenResponsePacket(final boolean canAdd, final boolean canRemove, final boolean canModify) {
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.canAdd = buf.readBoolean();
        this.canRemove = buf.readBoolean();
        this.canModify = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.canAdd);
        buf.writeBoolean(this.canRemove);
        buf.writeBoolean(this.canModify);
    }
}
