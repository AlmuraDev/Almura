/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class ClientboundPlayerNotificationPacket implements Message {

    public Text title;
    public Text message;
    public int timeToLive = -1;
    public boolean inWindow = false;

    public ClientboundPlayerNotificationPacket() {}

    public ClientboundPlayerNotificationPacket(Text title, Text message, int timeToLive) {
        this.title = title;
        this.message = message;
        this.timeToLive = timeToLive;
    }

    public ClientboundPlayerNotificationPacket(Text title, Text message) {
        this.title = title;
        this.message = message;
        this.inWindow = true;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.title = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString());
        this.message = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString());
        this.inWindow = buf.readBoolean();
        if (!this.inWindow) {
            this.timeToLive = buf.readInteger();
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.title));
        buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.message));
        buf.writeBoolean(this.inWindow);
        if (!this.inWindow) {
            buf.writeInteger(this.timeToLive);
        }
    }
}
