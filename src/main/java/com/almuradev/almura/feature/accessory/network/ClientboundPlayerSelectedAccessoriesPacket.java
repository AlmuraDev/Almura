/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundPlayerSelectedAccessoriesPacket implements Message {

    @Nullable public UUID uniqueId;
    @Nullable public Set<String> accessories;

    public ClientboundPlayerSelectedAccessoriesPacket() {}

    public ClientboundPlayerSelectedAccessoriesPacket(final UUID uniqueId, @Nullable final Set<String> accessories) {
        checkNotNull(uniqueId);

        this.uniqueId = uniqueId;
        this.accessories = accessories;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.uniqueId = buf.readUniqueId();

        final int count = buf.readInteger();

        if (count > 0) {
            this.accessories = new HashSet<>();

            for (int i = 0; i < count; i++) {
                this.accessories.add(buf.readString());
            }
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        checkNotNull(this.uniqueId);

        buf.writeUniqueId(this.uniqueId);
        buf.writeInteger(this.accessories == null ? 0 : this.accessories.size());

        if (this.accessories != null) {
            this.accessories.forEach(buf::writeString);
        }
    }
}
