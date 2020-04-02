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

public final class ServerboundMembershipPurchaseRequestPacket implements Message {

    public int membershipLevel, type;

    public ServerboundMembershipPurchaseRequestPacket(){}

    public ServerboundMembershipPurchaseRequestPacket(int membershipLevel, int type) {
        this.membershipLevel = membershipLevel;
        this.type = type;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.membershipLevel = buf.readInteger();
        this.type = buf.readInteger();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
       buf.writeInteger(this.membershipLevel);
       buf.writeInteger(this.type);
    }
}
