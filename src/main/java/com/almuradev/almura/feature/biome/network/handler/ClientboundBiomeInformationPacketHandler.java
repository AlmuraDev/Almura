/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.network.handler;

import com.almuradev.almura.feature.biome.BiomeClientFeature;
import com.almuradev.almura.feature.biome.network.ClientboundBiomeInformationPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundBiomeInformationPacketHandler implements MessageHandler<ClientboundBiomeInformationPacket> {

    private final BiomeClientFeature registry;

    @Inject
    public ClientboundBiomeInformationPacketHandler(final BiomeClientFeature registry) {
        this.registry = registry;
    }

    @Override
    public void handleMessage(ClientboundBiomeInformationPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                this.registry.setBiomeConfigs(message.biomes);
            }
        }
    }
}
