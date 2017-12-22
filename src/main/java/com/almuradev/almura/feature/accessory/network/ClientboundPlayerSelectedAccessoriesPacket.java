/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClientboundPlayerSelectedAccessoriesPacket implements Message {

    public UUID uniqueId;
    public Set<String> accessories;

    public ClientboundPlayerSelectedAccessoriesPacket() {}

    public ClientboundPlayerSelectedAccessoriesPacket(UUID uniqueId, Set<String> accessories) {
        this.uniqueId = uniqueId;
        this.accessories = accessories;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.accessories = new HashSet<>();

        this.uniqueId = buf.readUniqueId();

        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.accessories.add(buf.readString());
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeUniqueId(this.uniqueId);
        buf.writeInteger(this.accessories.size());

        this.accessories.forEach(buf::writeString);
    }
}
