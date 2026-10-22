/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network.handler;

import com.almuradev.almura.feature.bank.ServerBankManager;
import com.almuradev.almura.feature.bank.network.ServerboundBankActionPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundBankActionPacketHandler implements MessageHandler<ServerboundBankActionPacket> {

    private final ServerBankManager bankManager;

    @Inject
    public ServerboundBankActionPacketHandler(final ServerBankManager bankManager) {
        this.bankManager = bankManager;
    }

    @Override
    public void handleMessage(final ServerboundBankActionPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && message.action != null && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            this.bankManager.handleAction(((PlayerConnection) connection).getPlayer(), message);
        }
    }
}
