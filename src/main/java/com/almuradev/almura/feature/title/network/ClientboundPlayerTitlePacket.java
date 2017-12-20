package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

public final class ClientboundPlayerTitlePacket implements Message {

    public UUID uniqueId;
    public boolean add;
    public Text title;

    public ClientboundPlayerTitlePacket() {
    }

    public ClientboundPlayerTitlePacket(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.add = false;
        this.title = null;
    }

    public ClientboundPlayerTitlePacket(UUID uniqueId, Text title) {
        this.uniqueId = uniqueId;
        this.add = true;
        this.title = title;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.uniqueId = buf.readUniqueId();
        this.add = buf.readBoolean();
        if (this.add) {
            this.title = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString());
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeUniqueId(this.uniqueId);
        buf.writeBoolean(this.add);
        if (this.add) {
            buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.title));
        }
    }
}
