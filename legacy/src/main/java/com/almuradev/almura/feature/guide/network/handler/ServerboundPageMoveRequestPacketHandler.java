/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.ServerPageManager;
import com.almuradev.almura.feature.guide.network.ServerboundPageMoveRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundPageMoveRequestPacketHandler implements MessageHandler<ServerboundPageMoveRequestPacket> {

    private final ServerPageManager manager;

    @Inject
    public ServerboundPageMoveRequestPacketHandler(final ServerPageManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPageMoveRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {
            final MinecraftServer server = (MinecraftServer) Sponge.getServer();
            if (PacketUtil.checkThreadAndEnqueue(server, message, this, connection, side)) {
                this.manager.movePage(((PlayerConnection) connection).getPlayer(), message.id, message.direction);
            }
        }
    }
}
