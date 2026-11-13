/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network.handler;

import com.almuradev.almura.feature.accessory.ServerAccessoryManager;
import com.almuradev.almura.feature.accessory.network.ServerboundAccessoryGuiRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundAccessoryGuiRequestPacketHandler implements MessageHandler<ServerboundAccessoryGuiRequestPacket> {

    private final ServerAccessoryManager manager;

    @Inject
    private ServerboundAccessoryGuiRequestPacketHandler(final ServerAccessoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(final ServerboundAccessoryGuiRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection
                && PacketUtil.checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            this.manager.openGui(((PlayerConnection) connection).getPlayer());
        }
    }
}
