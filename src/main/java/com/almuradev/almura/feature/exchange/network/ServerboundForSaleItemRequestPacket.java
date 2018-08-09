/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import javax.annotation.Nullable;

public final class ServerboundForSaleItemRequestPacket implements Message {

  @Nullable public String id;
  public int itemRecNo;
  public boolean remove;

  public ServerboundForSaleItemRequestPacket() {
  }

  public ServerboundForSaleItemRequestPacket(final String id, final int itemRecNo, boolean remove) {
    checkNotNull(id);
    checkState(itemRecNo >= 0);

    this.id = id;
    this.itemRecNo = itemRecNo;
    this.remove =remove;
  }

  @Override
  public void readFrom(final ChannelBuf buf) {
    this.id = buf.readString();
    this.itemRecNo = buf.readVarInt();
    this.remove = buf.readBoolean();
  }

  @Override
  public void writeTo(final ChannelBuf buf) {
    checkNotNull(this.id);
    checkState(this.itemRecNo >= 0);

    buf.writeString(this.id);
    buf.writeVarInt(this.itemRecNo);
    buf.writeBoolean(this.remove);
  }
}
