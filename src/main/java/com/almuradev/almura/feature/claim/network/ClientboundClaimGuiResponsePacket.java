/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundClaimGuiResponsePacket implements Message {

    public boolean isOwner, isTrusted, isAdmin;

    public ClientboundClaimGuiResponsePacket() {
    }

    public ClientboundClaimGuiResponsePacket(final boolean isOwner, final boolean isTrusted, final boolean isAdmin) {
        this.isOwner = isOwner;
        this.isTrusted = isTrusted;
        this.isAdmin = isAdmin;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.isOwner = buf.readBoolean();
        this.isTrusted = buf.readBoolean();
        this.isAdmin = buf.readBoolean();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.isOwner);
        buf.writeBoolean(this.isAdmin);
        buf.writeBoolean(this.isAdmin);
    }
}
