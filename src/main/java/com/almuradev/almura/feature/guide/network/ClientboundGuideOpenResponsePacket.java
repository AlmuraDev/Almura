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

    public GuideOpenType type;

    // Global permissions
    public boolean canAdd;
    public boolean canRemove;
    public boolean canModify;
    public boolean requestedFromClient;

    public ClientboundGuideOpenResponsePacket() {
    }

    public ClientboundGuideOpenResponsePacket(GuideOpenType type, final boolean canAdd, final boolean canRemove, final boolean canModify, final boolean requestedFromClient) {
        this.type = type;
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
        this.requestedFromClient = requestedFromClient;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.type = GuideOpenType.of(buf.readByte());
        this.canAdd = buf.readBoolean();
        this.canRemove = buf.readBoolean();
        this.canModify = buf.readBoolean();
        this.requestedFromClient = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeByte((byte) this.type.ordinal());
        buf.writeBoolean(this.canAdd);
        buf.writeBoolean(this.canRemove);
        buf.writeBoolean(this.canModify);
        buf.writeBoolean(this.requestedFromClient);
    }
}
