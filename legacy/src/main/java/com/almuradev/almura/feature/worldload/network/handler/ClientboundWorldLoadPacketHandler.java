/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.network.handler;

import com.almuradev.almura.feature.worldload.client.ClientWorldLoadManager;
import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoadPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundWorldLoadPacketHandler implements MessageHandler<ClientboundWorldLoadPacket> {

    private final ClientWorldLoadManager worldLoadManager;

    @Inject
    private ClientboundWorldLoadPacketHandler(final ClientWorldLoadManager worldLoadManager) {
        this.worldLoadManager = worldLoadManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundWorldLoadPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                if (message.started) {
                    this.worldLoadManager.onLoadStarted(message.worldName);
                } else {
                    this.worldLoadManager.onLoadFinished(message.worldName);
                }
            }
        }
    }
}
