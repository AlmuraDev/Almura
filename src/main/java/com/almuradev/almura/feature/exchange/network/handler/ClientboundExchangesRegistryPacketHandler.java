/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundExchangesRegistryPacketHandler implements MessageHandler<ClientboundExchangeRegistryPacket> {

    private final ClientExchangeManager exchangeManager;

    @Inject
    public ClientboundExchangesRegistryPacketHandler(final ClientExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundExchangeRegistryPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {

            this.exchangeManager.putExchanges(message.exchanges);

            final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

            // TODO Manage Exchange screen
        }
    }
}
