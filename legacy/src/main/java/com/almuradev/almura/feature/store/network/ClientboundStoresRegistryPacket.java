/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.basic.BasicStore;
import com.almuradev.almura.shared.util.SerializationUtil;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ClientboundStoresRegistryPacket implements Message {

    @Nullable public Set<Store> stores;

    public ClientboundStoresRegistryPacket() {
    }

    public ClientboundStoresRegistryPacket(@Nullable final Set<Store> stores) {
        this.stores = stores;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        final int count = buf.readInteger();

        if (count > 0) {
            this.stores = new HashSet<>();

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
                final BasicStore store = new BasicStore(id, created, creator, name, permission, isHidden);
                store.setCreatorName(creatorName);

                this.stores.add(store);
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.stores == null ? 0 : this.stores.size());

        if (this.stores != null) {
            for (final Store store : this.stores) {
                buf.writeString(store.getId());
                buf.writeString(store.getName());

                try {
                    final byte[] createdData = SerializationUtil.objectToBytes(store.getCreated());
                    buf.writeInteger(createdData.length);
                    buf.writeBytes(createdData);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final byte[] creatorData = SerializationUtil.toBytes(store.getCreator());
                buf.writeInteger(creatorData.length);
                buf.writeBytes(creatorData);

                final String creatorName = store.getCreatorName().orElse(null);
                buf.writeBoolean(creatorName != null);
                if (creatorName != null) {
                    buf.writeString(creatorName);
                }

                buf.writeString(store.getPermission());
                buf.writeBoolean(store.isHidden());
            }
        }
    }
}
