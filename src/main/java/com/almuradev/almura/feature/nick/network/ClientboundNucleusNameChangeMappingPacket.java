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

    public ClientboundNucleusNameChangeMappingPacket(final UUID uuid, final Text text) {
        this.uuid = uuid;
        this.text = text;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.uuid = buf.readUniqueId();
        this.text = TextSerializers.JSON.deserialize(buf.readString());
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeUniqueId(this.uuid);
        buf.writeString(TextSerializers.JSON.serialize(this.text));
    }
}
