/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ClientboundListItemsResponsePacketHandler implements MessageHandler<ClientboundListItemsResponsePacket> {

    private final ClientExchangeManager exchangeManager;

    @Inject
    public ClientboundListItemsResponsePacketHandler(final ClientExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundListItemsResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            this.exchangeManager.handleExchangeListItems(message.id, message.listItems);
        }
    }
}
