/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

// TODO May need no response packet for this one
public final class ServerboundExchangeTransactionRequestPacket implements Message {

  public ServerboundExchangeTransactionRequestPacket() {
  }

  @Override
  public void readFrom(final ChannelBuf buf) {

  }

  @Override
  public void writeTo(final ChannelBuf buf) {

  }
}
