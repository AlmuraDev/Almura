package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashSet;
import java.util.Set;

public final class ClientboundPlayerTitlesResponsePacket implements Message {

    public Set<Text> titles;

    public ClientboundPlayerTitlesResponsePacket() {}

    public ClientboundPlayerTitlesResponsePacket(Set<Text> titles) {
        this.titles = titles;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.titles = new HashSet<>();

        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.titles.add(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(buf.readString()));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        final int count = this.titles.size();

        buf.writeInteger(count);
        for (Text title : this.titles) {
            buf.writeString(TextSerializers.LEGACY_FORMATTING_CODE.serialize(title));
        }
    }
}
