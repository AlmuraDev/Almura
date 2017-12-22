package com.almuradev.almura.feature.notification.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class ClientboundPlayerNotificationPacket implements Message {

    public Text message;
    public int timeToLive = -1;
    public boolean inWindow = false;

    public ClientboundPlayerNotificationPacket() {}

    public ClientboundPlayerNotificationPacket(Text message, int timeToLive) {
        this.message = message;
        this.timeToLive = timeToLive;
    }

    public ClientboundPlayerNotificationPacket(Text message) {
        this.message = message;
        this.inWindow = true;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.message = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString());
        this.inWindow = buf.readBoolean();
        if (!this.inWindow) {
            this.timeToLive = buf.readInteger();
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.message));
        buf.writeBoolean(this.inWindow);
        if (!this.inWindow) {
            buf.writeInteger(this.timeToLive);
        }
    }
}
