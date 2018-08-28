/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.network;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@SuppressWarnings("deprecation")
public final class ClientboundPlayerNotificationPacket implements Message {

    public String title;
    public String message;
    public int timeToLive = -1;
    public boolean inWindow = false;

    public ClientboundPlayerNotificationPacket() {
    }

    public ClientboundPlayerNotificationPacket(final Text title, final Text message) {
        this(title, message, -1);
        this.inWindow = true;
    }

    public ClientboundPlayerNotificationPacket(final Text title, final Text message, int timeToLive) {
        checkNotNull(title);
        checkNotNull(message);

        this.title = TextSerializers.LEGACY_FORMATTING_CODE.serialize(title);
        this.message = TextSerializers.LEGACY_FORMATTING_CODE.serialize(message);
        this.timeToLive = timeToLive;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.title = buf.readString();
        this.message = buf.readString();
        this.inWindow = buf.readBoolean();
        if (!this.inWindow) {
            this.timeToLive = buf.readInteger();
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.title);
        buf.writeString(this.message);
        buf.writeBoolean(this.inWindow);
        if (!this.inWindow) {
            buf.writeInteger(this.timeToLive);
        }
    }
}
