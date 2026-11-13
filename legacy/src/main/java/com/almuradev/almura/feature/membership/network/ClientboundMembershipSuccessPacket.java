/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundMembershipSuccessPacket implements Message {

    public String membership;

    public ClientboundMembershipSuccessPacket () {}

    public ClientboundMembershipSuccessPacket(String membership){
        this.membership = membership;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.membership = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.membership);
    }
}
