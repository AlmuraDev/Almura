/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundFeaturesOpenResponsePacket implements Message {

    public boolean admin;

    public ClientboundFeaturesOpenResponsePacket() {
    }

    public ClientboundFeaturesOpenResponsePacket(final boolean admin) {
        this.admin = admin;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.admin = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.admin);
    }
}
