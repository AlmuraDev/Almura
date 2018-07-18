/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network.handler;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerCountPacketHandler implements MessageHandler<ClientboundPlayerCountPacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundPlayerCountPacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @Override
    public void handleMessage(ClientboundPlayerCountPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                this.hudData.onlinePlayerCount = message.online;
                this.hudData.maxPlayerCount = message.max;
            }
        }
    }
}
