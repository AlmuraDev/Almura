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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Opens the accessory screen: the types the player may wear and what they wear now.
 */
public final class ClientboundAccessoryGuiPacket implements Message {

    public List<AccessoryType> allowed = new ArrayList<>();
    public Map<AccessoryType, String> worn = new EnumMap<>(AccessoryType.class);

    public ClientboundAccessoryGuiPacket() {
    }

    public ClientboundAccessoryGuiPacket(final List<AccessoryType> allowed, final Map<AccessoryType, String> worn) {
        this.allowed = allowed;
        this.worn = worn;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        final int count = buf.readInteger();
        this.allowed = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final AccessoryType type = AccessoryType.byId(buf.readString());
            if (type != null) {
                this.allowed.add(type);
            }
        }
        this.worn = AccessoryBuf.readWorn(buf);
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.allowed.size());
        for (final AccessoryType type : this.allowed) {
            buf.writeString(type.id());
        }
        AccessoryBuf.writeWorn(buf, this.worn);
    }
}
