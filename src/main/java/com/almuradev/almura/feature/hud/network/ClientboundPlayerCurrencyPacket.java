/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;

public final class ClientboundPlayerCurrencyPacket implements Message {

    public BigDecimal money;

    public ClientboundPlayerCurrencyPacket() {}

    public ClientboundPlayerCurrencyPacket(BigDecimal money) {
        this.money = money;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.money = BigDecimal.valueOf(buf.readDouble());
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.money.doubleValue());
    }
}
