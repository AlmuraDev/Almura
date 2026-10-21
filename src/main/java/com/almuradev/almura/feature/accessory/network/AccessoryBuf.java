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

import java.util.EnumMap;
import java.util.Map;

/**
 * Reads and writes the accessories a player wears (type id and variant).
 */
final class AccessoryBuf {

    static void writeWorn(final ChannelBuf buf, final Map<AccessoryType, String> worn) {
        buf.writeInteger(worn.size());
        for (final Map.Entry<AccessoryType, String> entry : worn.entrySet()) {
            buf.writeString(entry.getKey().id());
            buf.writeString(entry.getValue());
        }
    }

    static Map<AccessoryType, String> readWorn(final ChannelBuf buf) {
        final Map<AccessoryType, String> worn = new EnumMap<>(AccessoryType.class);
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            final AccessoryType type = AccessoryType.byId(buf.readString());
            final String variant = buf.readString();
            if (type != null) {
                worn.put(type, variant);
            }
        }
        return worn;
    }

    private AccessoryBuf() {
    }
}
