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

    public int membershipLevel;

    public ServerboundMembershipPurchaseRequestPacket(){}

    public ServerboundMembershipPurchaseRequestPacket(int membershipLevel) {
        this.membershipLevel = membershipLevel;
        System.out.println("test");
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.membershipLevel = buf.readInteger();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
       buf.writeInteger(this.membershipLevel);
    }
}
