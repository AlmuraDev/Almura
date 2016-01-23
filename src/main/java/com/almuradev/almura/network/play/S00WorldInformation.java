/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network.play;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public class S00WorldInformation implements Message {
    public String worldName;

    public S00WorldInformation(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.worldName = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.worldName);
    }
}
