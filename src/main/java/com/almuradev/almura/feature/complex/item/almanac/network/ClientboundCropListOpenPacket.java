/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Opens the Farmer's Almanac crop list. The client builds the list from its own crop content, which matches the server's. The biome and
 * its ground temperature come from the server, as the client doesn't know TerrainControl's biomes, and so do the farming levels when the
 * server runs the Skills plugin.
 */
public final class ClientboundCropListOpenPacket implements Message {

    public String biomeId = "";
    public float groundTemperature;

    /** True when the server runs the Skills plugin and could read its farming levels */
    public boolean skills;
    /** The player's farming level, or -1 if not known */
    public int farmingLevel = -1;
    /** Crop block id to the farming level needed to plant it and to harvest it ({@code [plant, harvest]}, -1 when not needed) */
    public final Map<String, int[]> levels = new HashMap<>();

    public ClientboundCropListOpenPacket() {
    }

    public ClientboundCropListOpenPacket(final String biomeId, final float groundTemperature) {
        this.biomeId = biomeId;
        this.groundTemperature = groundTemperature;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.biomeId = buf.readString();
        this.groundTemperature = buf.readFloat();
        this.skills = buf.readBoolean();
        this.farmingLevel = buf.readInteger();
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            final String id = buf.readString();
            final int plant = buf.readInteger();
            final int harvest = buf.readInteger();
            this.levels.put(id, new int[] {plant, harvest});
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.biomeId);
        buf.writeFloat(this.groundTemperature);
        buf.writeBoolean(this.skills);
        buf.writeInteger(this.farmingLevel);
        buf.writeInteger(this.levels.size());
        for (final Map.Entry<String, int[]> entry : this.levels.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeInteger(entry.getValue()[0]);
            buf.writeInteger(entry.getValue()[1]);
        }
    }
}
