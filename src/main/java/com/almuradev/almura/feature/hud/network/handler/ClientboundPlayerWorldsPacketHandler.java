/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network.handler;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorldsPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerWorldsPacketHandler implements MessageHandler<ClientboundPlayerWorldsPacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundPlayerWorldsPacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @Override
    public void handleMessage(ClientboundPlayerWorldsPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                if (message.replace) {
                    this.hudData.playerWorlds.clear();
                }

                message.worlds.forEach((uuid, world) -> {
                    if (world.isEmpty()) {
                        this.hudData.playerWorlds.remove(uuid);
                    } else {
                        this.hudData.playerWorlds.put(uuid, world);
                    }
                });
            }
        }
    }
}
