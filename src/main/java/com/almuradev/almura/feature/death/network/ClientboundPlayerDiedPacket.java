/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundPlayerDiedPacket implements Message {

    public double dropAmount;

    public ClientboundPlayerDiedPacket(){}

    public ClientboundPlayerDiedPacket(double dropAmount) {
        this.dropAmount = dropAmount;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.dropAmount = buf.readDouble();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.dropAmount);
    }
}
