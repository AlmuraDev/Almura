/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsSaleStatusPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundListItemsSaleStatusPacketHandler implements MessageHandler<ClientboundListItemsSaleStatusPacket> {

    private final ClientExchangeManager exchangeManager;

    @Inject
    public ClientboundListItemsSaleStatusPacketHandler(final ClientExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundListItemsSaleStatusPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            this.exchangeManager.handleListItemsSaleStatus(message.id, message.serverListedItems, message.serverLastKnownPriceItems);
        }
    }
}
