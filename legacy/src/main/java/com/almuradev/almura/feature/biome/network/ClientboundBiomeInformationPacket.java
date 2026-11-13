/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.network;

import com.almuradev.almura.feature.biome.BiomeConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public final class ClientboundBiomeInformationPacket implements Message {

    public Map<Integer, BiomeConfig> biomes;

    public ClientboundBiomeInformationPacket() {
    }

    public ClientboundBiomeInformationPacket(final Map<Integer, BiomeConfig> biomes) {
        this.biomes = biomes;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {

        final int size = buf.readInteger();
        this.biomes = new HashMap<>(size);

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                final int length = buf.readInteger();

                ByteBufInputStream byteBufInputStream = null;
                ObjectInputStream objectInputStream = null;

                try {
                    final ByteBuf buffer = Unpooled.buffer(length);
                    buffer.writeBytes(buf.readBytes(length));
                    byteBufInputStream = new ByteBufInputStream(buffer, length);
                    objectInputStream = new ObjectInputStream(byteBufInputStream);
                    final BiomeConfig biomeConfig = (BiomeConfig) objectInputStream.readObject();
                    this.biomes.put(biomeConfig.getServerBiomeId(), biomeConfig);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (byteBufInputStream != null) {
                        try {
                            byteBufInputStream.close();
                        } catch (IOException ignored) {
                        }
                    }

                    if (objectInputStream != null) {
                        try {
                            objectInputStream.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        final int size = this.biomes.size();
        buf.writeInteger(size);

        if (size > 0) {
            for (final BiomeConfig biomeConfig : this.biomes.values()) {
                ByteBufOutputStream byteBufOutputStream = null;
                ObjectOutputStream objectOutputStream = null;

                try {
                    final ByteBuf objBuf = Unpooled.buffer();
                    byteBufOutputStream = new ByteBufOutputStream(objBuf);
                    objectOutputStream = new ObjectOutputStream(byteBufOutputStream);
                    objectOutputStream.writeObject(biomeConfig);
                    buf.writeInteger(objBuf.array().length);
                    buf.writeBytes(objBuf.array());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (byteBufOutputStream != null) {
                        try {
                            byteBufOutputStream.close();
                        } catch (IOException ignored) {
                        }
                    }

                    if (objectOutputStream != null) {
                        try {
                            objectOutputStream.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
    }
}
