/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network.handler;

import com.almuradev.almura.feature.exchange.ServerExchangeManager;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeTransactionRequestPacket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerboundExchangeTransactionRequestPacketHandler implements MessageHandler<ServerboundExchangeTransactionRequestPacket> {

  private final ServerExchangeManager exchangeManager;

  @Inject
  public ServerboundExchangeTransactionRequestPacketHandler(final ServerExchangeManager exchangeManager) {
    this.exchangeManager = exchangeManager;
  }

  @Override
  public void handleMessage(final ServerboundExchangeTransactionRequestPacket message, final RemoteConnection connection, final Platform.Type side) {

  }
}
