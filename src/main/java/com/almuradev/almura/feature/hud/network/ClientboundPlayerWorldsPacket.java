/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tells the client which world each online player is in, for the player list (tab).
 *
 * <p>When {@link #replace} is true the client discards what it knows and uses these entries (sent on join). Otherwise the entries are
 * merged in. An empty world name removes that player (sent on disconnect).</p>
 */
public final class ClientboundPlayerWorldsPacket implements Message {

    public boolean replace;
    public final Map<UUID, String> worlds = new HashMap<>();

    public ClientboundPlayerWorldsPacket() {
    }

    public ClientboundPlayerWorldsPacket(final boolean replace, final Map<UUID, String> worlds) {
        this.replace = replace;
        this.worlds.putAll(worlds);
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.replace = buf.readBoolean();
        final int count = buf.readInteger();
        this.worlds.clear();

        for (int i = 0; i < count; i++) {
            this.worlds.put(buf.readUniqueId(), buf.readString());
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.replace);
        buf.writeInteger(this.worlds.size());

        this.worlds.forEach((uuid, world) -> {
            buf.writeUniqueId(uuid);
            buf.writeString(world);
        });
    }
}
