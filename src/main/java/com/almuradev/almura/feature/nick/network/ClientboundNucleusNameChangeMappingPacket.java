package com.almuradev.almura.feature.nick.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

public final class ClientboundNucleusNameChangeMappingPacket implements Message {

    // Packet: UUID then Nickname

    public UUID uuid;
    public Text text;

    public ClientboundNucleusNameChangeMappingPacket() {
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.uuid = buf.readUniqueId();
        this.text = TextSerializers.JSON.deserialize(buf.readString());
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeUniqueId(this.uuid);
        buf.writeString(TextSerializers.JSON.serialize(this.text));
    }
}
