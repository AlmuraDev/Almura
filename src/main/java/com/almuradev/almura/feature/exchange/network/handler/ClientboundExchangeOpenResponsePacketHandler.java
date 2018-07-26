/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeOpenResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundExchangeOpenResponsePacketHandler implements MessageHandler<ClientboundExchangeOpenResponsePacket> {

    @Override
    public void handleMessage(ClientboundExchangeOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            final Minecraft client = Minecraft.getMinecraft();
            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {
                new ExchangeScreen().display();
            }
        }
    }
}
