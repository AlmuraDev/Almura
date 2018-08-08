/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.ServerExchangeManager;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeForSaleItemsRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerboundExchangeForSaleItemsRequestPacketHandler implements MessageHandler<ServerboundExchangeForSaleItemsRequestPacket> {

  private final ServerExchangeManager exchangeManager;

  @Inject
  public ServerboundExchangeForSaleItemsRequestPacketHandler(final ServerExchangeManager exchangeManager) {
    this.exchangeManager = exchangeManager;
  }

  @Override
  public void handleMessage(final ServerboundExchangeForSaleItemsRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
    if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
      .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

    }
  }
}
