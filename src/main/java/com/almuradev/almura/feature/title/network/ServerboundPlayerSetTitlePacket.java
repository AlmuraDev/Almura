package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundPlayerSetTitlePacket implements Message {

    public String title;
    public boolean add;

    public ServerboundPlayerSetTitlePacket() {
        this.add = false;
    }

    public ServerboundPlayerSetTitlePacket(String title) {
        this.add = true;
        this.title = title;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.add = buf.readBoolean();
        if (this.add) {
            this.title = buf.readString();
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.add);
        if (this.add) {
            buf.writeString(this.title);
        }
    }
}
