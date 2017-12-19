package com.almuradev.almura.feature.nick.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientboundNucleusNameMappingsPacket implements Message {

    // Number of entries, nicknames

    public final Map<UUID, Text> nicknames = new HashMap<>();

    public ClientboundNucleusNameMappingsPacket() {
    }

    public ClientboundNucleusNameMappingsPacket(final Map<UUID, Text> nicknames) {
        this.nicknames.putAll(nicknames);
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        final int count = buf.readInteger();
        this.nicknames.clear();

        for (int i = 0; i < count; i++) {
            this.nicknames.put(buf.readUniqueId(), TextSerializers.JSON.deserialize(buf.readString()));
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.nicknames.size());

        this.nicknames.forEach((uuid, text) -> {
            buf.writeUniqueId(uuid);
            buf.writeString(TextSerializers.JSON.serialize(text));
        });
    }
}
