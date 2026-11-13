/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import com.almuradev.almura.feature.accessory.AccessoryType;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * The accessories a player wears (empty when none, or when they left).
 */
public final class ClientboundPlayerAccessoriesPacket implements Message {

    public UUID player;
    public Map<AccessoryType, String> worn = new EnumMap<>(AccessoryType.class);

    public ClientboundPlayerAccessoriesPacket() {
    }

    public ClientboundPlayerAccessoriesPacket(final UUID player, final Map<AccessoryType, String> worn) {
        this.player = player;
        this.worn = worn;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.player = buf.readUniqueId();
        this.worn = AccessoryBuf.readWorn(buf);
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeUniqueId(this.player);
        AccessoryBuf.writeWorn(buf, this.worn);
    }
}
