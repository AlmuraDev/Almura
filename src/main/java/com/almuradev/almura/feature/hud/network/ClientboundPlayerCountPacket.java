/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public class ClientboundPlayerCountPacket implements Message {

    public int online;
    public int max;

    public ClientboundPlayerCountPacket() {
    }

    public ClientboundPlayerCountPacket(final int online, final int max) {
        this.online = online;
        this.max = max;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.online = buf.readInteger();
        this.max = buf.readInteger();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.online);
        buf.writeInteger(this.max);
    }
}
