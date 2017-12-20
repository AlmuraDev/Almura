package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundPlayerSetTitlePacket implements Message {

    public String title;

    public ServerboundPlayerSetTitlePacket() {}

    public ServerboundPlayerSetTitlePacket(String title) {
        this.title = title;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.title = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.title);
    }
}
