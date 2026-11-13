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

public final class ServerboundMembershipGuiOpenRequestPacket implements Message {

    public ServerboundMembershipGuiOpenRequestPacket(){}

    @Override
    public void readFrom(ChannelBuf buf) {
        // Nothing to read yet.
    }

    @Override
    public void writeTo(ChannelBuf buf) {
       // Nothing to write yet.
    }
}
