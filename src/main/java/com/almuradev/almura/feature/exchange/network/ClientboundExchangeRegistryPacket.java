/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.feature.exchange.BasicExchange;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.shared.util.SerializationUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        final int count = buf.readInteger();

        if (count > 0) {
            this.exchanges = new HashSet<>();

            for (int i = 0; i < count; i++) {
                final String id = buf.readString();
                final String name = buf.readString();

                final Instant created;
                try {
                    created = SerializationUtil.bytesToObject(buf.readBytes(buf.readInteger()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                final UUID creator = SerializationUtil.uniqueIdFromBytes(buf.readBytes(buf.readInteger()));
                final String creatorName = buf.readBoolean() ? buf.readString() : null;
                final String permission = buf.readString();
                final boolean isHidden = buf.readBoolean();
                final BasicExchange axs = new BasicExchange(id, created, creator, name, permission, isHidden);

                this.exchanges.add(axs);

                if (Sponge.getPlatform().getExecutionType().isClient()) {
                    axs.setCreatorName(creatorName);
                }
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.exchanges == null ? 0 : this.exchanges.size());

        if (this.exchanges != null) {
            for (final Exchange axs : this.exchanges) {
                buf.writeString(axs.getId());
                buf.writeString(axs.getName());

                try {
                    final byte[] createdData = SerializationUtil.objectToBytes(axs.getCreated());
                    buf.writeInteger(createdData.length);
                    buf.writeBytes(createdData);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final byte[] creatorData = SerializationUtil.toBytes(axs.getCreator());
                buf.writeInteger(creatorData.length);
                buf.writeBytes(creatorData);

                final String creatorName = axs.getCreatorName().orElse(null);
                buf.writeBoolean(creatorName != null);
                if (creatorName != null) {
                    buf.writeString(creatorName);
                }

                buf.writeString(axs.getPermission());
                buf.writeBoolean(axs.isHidden());
            }
        }
    }
}
