/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundBiomeChunkDataPacket implements Message {

    public long chunkKey;
    public int[] biomeArray;

    public ClientboundBiomeChunkDataPacket() {
    }

    public ClientboundBiomeChunkDataPacket(final long chunkKey) {
        this.chunkKey = chunkKey;
        this.biomeArray = null;
    }

    public ClientboundBiomeChunkDataPacket(final long chunkKey, final int[] biomeArray) {
        this.chunkKey = chunkKey;
        this.biomeArray = biomeArray;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.chunkKey = buf.readLong();
        if (!buf.readBoolean()) {
            this.biomeArray = new int[16 * 16];
            for (int i = 0; i < this.biomeArray.length; i++) {
                this.biomeArray[i] = buf.readVarInt();
            }
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeLong(this.chunkKey);
        buf.writeBoolean(this.biomeArray == null);
        if (this.biomeArray != null) {
            for (final int biomeId : this.biomeArray) {
                buf.writeVarInt(biomeId);
            }
        }
    }
}
