/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.shared.util.PacketUtil;
import com.almuradev.almura.shared.util.SerializationUtil;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public final class ClientboundExchangeRegistryPacket implements Message {

    @Nullable public Set<Exchange> exchanges;

    public ClientboundExchangeRegistryPacket() {
    }

    public ClientboundExchangeRegistryPacket(@Nullable final Set<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        final int count = buf.readVarInt();

        if (count > 0) {
            this.exchanges = new HashSet<>();

            for (int i = 0; i < count; i++) {
                try {
                    this.exchanges.add(SerializationUtil.bytesToObject(buf.readBytes(buf.readVarInt())));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeVarInt(this.exchanges == null ? 0 : this.exchanges.size());

        if (this.exchanges != null) {
            for (Exchange exchange : this.exchanges) {
                try {
                    final byte[] data = SerializationUtil.objectToBytes(exchange);
                    buf.writeVarInt(data.length);
                    buf.writeBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
